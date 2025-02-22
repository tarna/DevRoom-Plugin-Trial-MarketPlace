package dev.tarna.marketplace.api.marketplace

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import dev.tarna.marketplace.MarketPlacePlugin
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.database.models.Transaction
import dev.tarna.marketplace.api.utils.serializeItem
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

object Marketplace {
    private val plugin = MarketPlacePlugin.instance
    private val redis = MarketPlacePlugin.redisClient
    private val itemsCollection = MarketPlacePlugin.itemsCollection

    suspend fun sell(player: Player, price: Double) {
        val item = player.inventory.itemInMainHand
        itemsCollection.insertOne(
            MarketplaceItem(
                seller = player.uniqueId,
                serializedItem = serializeItem(item),
                price = price
            )
        )
        player.inventory.setItemInMainHand(null)

        val cache = redis.get("marketplace:false")
        if (cache != null) {
            val data = Json.decodeFromString<MutableList<MarketplaceItem>>(cache)
            data.add(
                MarketplaceItem(
                    seller = player.uniqueId,
                    serializedItem = serializeItem(item),
                    price = price
                )
            )
            redis.set("marketplace:false", Json.encodeToString(data))
        } else {
            redis.set("marketplace:false", Json.encodeToString(listOf(
                MarketplaceItem(
                    seller = player.uniqueId,
                    serializedItem = serializeItem(item),
                    price = price
                )
            )))
        }
    }

    suspend fun getItems(blackmarket: Boolean = false): List<MarketplaceItem> {
        val cached = redis.get("marketplace:$blackmarket")
        if (cached != null) return Json.decodeFromString(cached)

        val data = itemsCollection.find(
            eq("blackmarket", blackmarket)
        ).toList()
        redis.set("marketplace:$blackmarket", Json.encodeToString(data))
        return data
    }

    suspend fun buyItem(buyer: Player, seller: OfflinePlayer, marketplaceItem: MarketplaceItem) {
        Economy.remove(buyer, if (marketplaceItem.blackmarket) marketplaceItem.price / 2 else marketplaceItem.price)
        Economy.add(seller, if (marketplaceItem.blackmarket) marketplaceItem.price * 2 else marketplaceItem.price)
        itemsCollection.deleteOne(eq("id", marketplaceItem.id))

        val itemsCache = redis.get("marketplace:${marketplaceItem.blackmarket}")
        if (itemsCache != null) {
            val data = Json.decodeFromString<MutableList<MarketplaceItem>>(itemsCache)
            data.remove(marketplaceItem)
            redis.set("marketplace:${marketplaceItem.blackmarket}", Json.encodeToString(data))
        } else {
            val data = getItems(marketplaceItem.blackmarket).toMutableList()
            data.remove(marketplaceItem)
            redis.set("marketplace:${marketplaceItem.blackmarket}", Json.encodeToString(data))
        }

        val transaction = Transaction(
            seller = seller.uniqueId,
            buyer = buyer.uniqueId,
            serializedItem = marketplaceItem.serializedItem,
            price = marketplaceItem.price,
            blackmarket = marketplaceItem.blackmarket
        )
        transaction.log()
        Transactions.storeTransaction(transaction)

        val item = marketplaceItem.item ?: return
        buyer.inventory.addItem(item)
    }

    fun scheduleBlackmarketRefresh() {
        Bukkit.getServer().asyncScheduler.runAtFixedRate(plugin, {
            plugin.launch {
                refreshBlackmarket()
            }
        }, 0, 1, TimeUnit.HOURS)
    }

    suspend fun refreshBlackmarket() {
        itemsCollection.updateMany(
            eq("blackmarket", true),
            set("blackmarket", false)
        )

        val items = getItems().shuffled()
        val size = items.size
        val randomItems = getItems().shuffled().take(if (size > 7) 7 else size)
        for (item in randomItems) {
            itemsCollection.updateOne(
                eq("id", item.id.toString()),
                set("blackmarket", true)
            )
        }

        redis.del("marketplace:true")
        redis.del("marketplace:false")

        getItems(true)
        getItems(false)
    }
}