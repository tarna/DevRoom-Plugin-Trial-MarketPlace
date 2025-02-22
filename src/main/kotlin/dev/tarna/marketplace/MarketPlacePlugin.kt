package dev.tarna.marketplace

import club.minnced.discord.webhook.WebhookClient
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.database.models.Transaction
import dev.tarna.marketplace.api.marketplace.Marketplace
import dev.tarna.marketplace.api.utils.timedDisabled
import dev.tarna.marketplace.api.utils.timedEnabled
import dev.tarna.marketplace.commands.MarketplaceCommand
import dev.tarna.marketplace.commands.SellCommand
import dev.tarna.marketplace.commands.TransactionsCommand
import io.github.crackthecodeabhi.kreds.connection.*
import net.milkbowl.vault.economy.Economy
import org.bson.UuidRepresentation
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source

class MarketPlacePlugin : JavaPlugin() {
    companion object {
        val instance by lazy { getPlugin(MarketPlacePlugin::class.java) }

        lateinit var mongoClient: MongoClient
        lateinit var database: MongoDatabase
        lateinit var itemsCollection: MongoCollection<MarketplaceItem>
        lateinit var transactionsCollection: MongoCollection<Transaction>

        lateinit var redisClient: KredsClient
        lateinit var webhookClient: WebhookClient

        lateinit var economy: Economy
        lateinit var commandManager: PaperCommandManager<Source>
    }

    override fun onEnable() {
        timedEnabled("Plugin") {
            saveDefaultConfig()
            if (!setupEconomy()) return@timedEnabled false
            setupDatabase()
            setupRedis()
            setupWebhook()
            loadCommands()

            Marketplace.scheduleBlackmarketRefresh()
            true
        }
    }

    override fun onDisable() {
        timedDisabled("Plugin") {
            mongoClient.close()
            true
        }
    }

    private fun setupEconomy(): Boolean {
        return timedEnabled("Vault") {
            if (server.pluginManager.getPlugin("Vault") == null) {
                logger.severe("Vault not found! Disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return false
            }
            val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
            economy = rsp.provider
            return true
        }
    }

    @Suppress("UnstableApiUsage")
    private fun loadCommands() {
        timedEnabled("Commands") {
            commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(this)
            val annotationParser = AnnotationParser(commandManager, Source::class.java)
            annotationParser.installCoroutineSupport()

            val commands = listOf(
                MarketplaceCommand(),
                SellCommand(),
                TransactionsCommand()
            )
            commands.forEach { annotationParser.parse(it) }

            true
        }
    }

    private fun setupDatabase() {
        timedEnabled("Mongo Database") {
            val mongoUrl = config.getString("mongo-url") ?: run {
                logger.severe("Mongo URL not found in config! Disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return
            }
            mongoClient = MongoClient.create(
                MongoClientSettings.builder()
                    .applyConnectionString(ConnectionString(mongoUrl))
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .build()
            )

            database = mongoClient.getDatabase("marketplace")
            itemsCollection = database.getCollection<MarketplaceItem>("items")
            transactionsCollection = database.getCollection<Transaction>("transactions")
            true
        }
    }

    private fun setupRedis() {
        timedEnabled("Redis") {
            val redisUrl = config.getString("redis-url") ?: run {
                logger.severe("Redis URL not found in config! Disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return
            }
//            val endpoint = Endpoint.from(redisUrl)
            val endpoint = Endpoint("localhost", 6379)
            redisClient = newClient(endpoint)
            true
        }
    }

    private fun setupWebhook() {
        timedEnabled("Webhook") {
            val webhookUrl = config.getString("webhook-url") ?: run {
                logger.severe("Webhook URL not found in config! Disabling plugin.")
                server.pluginManager.disablePlugin(this)
                return
            }
            webhookClient = WebhookClient.withUrl(webhookUrl)
            true
        }
    }
}