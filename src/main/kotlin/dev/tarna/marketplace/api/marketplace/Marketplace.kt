package dev.tarna.marketplace.api.marketplace

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import dev.tarna.marketplace.MarketPlacePlugin
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.database.models.Transaction
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
    private val transactionsCollection = MarketPlacePlugin.transactionsCollection

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
        transactionsCollection.insertOne(Transaction(
            seller = seller.uniqueId,
            buyer = buyer.uniqueId,
            serializedItem = marketplaceItem.serializedItem,
            price = marketplaceItem.price,
            blackmarket = marketplaceItem.blackmarket
        ))
        val item = marketplaceItem.item ?: return
        buyer.give(item)
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

        val randomItems = getItems().shuffled().take(7)
        for (item in randomItems) {
            itemsCollection.updateOne(
                eq("id", item.id),
                set("blackmarket", true)
            )
        }
    }
}