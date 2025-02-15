package dev.tarna.marketplace.api.marketplace

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import dev.tarna.marketplace.MarketPlacePlugin
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import kotlinx.coroutines.flow.toList
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

object Marketplace {
    private val plugin = MarketPlacePlugin.instance
    private val itemsCollection = MarketPlacePlugin.itemsCollection
    private val economy = MarketPlacePlugin.economy


    suspend fun getItems(blackmarket: Boolean = false): List<MarketplaceItem> {
        return itemsCollection.find(
            eq("blackmarket", blackmarket)
        ).toList()
    }

    suspend fun buyItem(buyer: Player, seller: OfflinePlayer, marketplaceItem: MarketplaceItem) {
        Economy.remove(buyer, if (marketplaceItem.blackmarket) marketplaceItem.price / 2 else marketplaceItem.price)
        Economy.add(seller, if (marketplaceItem.blackmarket) marketplaceItem.price * 2 else marketplaceItem.price)
        itemsCollection.deleteOne(eq("id", marketplaceItem.id))
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