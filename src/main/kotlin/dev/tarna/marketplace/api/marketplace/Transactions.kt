package dev.tarna.marketplace.api.marketplace

import com.mongodb.client.model.Filters.eq
import dev.tarna.marketplace.MarketPlacePlugin
import dev.tarna.marketplace.api.database.models.Transaction
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player

object Transactions {
    private val transactionsCollection = MarketPlacePlugin.transactionsCollection
    private val redis = MarketPlacePlugin.redisClient

    suspend fun getBuyerTransactions(player: Player): List<Transaction> {
        val cached = redis.get("transactions:buyer:${player.uniqueId}")
        if (cached != null) return Json.decodeFromString(cached)

        val data = transactionsCollection.find(
            eq("buyer", player.uniqueId)
        ).toList()
        redis.set("transactions:buyer:${player.uniqueId}", Json.encodeToString(data))
        return data
    }

    suspend fun getSellerTransactions(player: Player): List<Transaction> {
        val cached = redis.get("transactions:seller:${player.uniqueId}")
        if (cached != null) return Json.decodeFromString(cached)

        val data = transactionsCollection.find(
            eq("seller", player.uniqueId)
        ).toList()
        redis.set("transactions:seller:${player.uniqueId}", Json.encodeToString(data))
        return data
    }
}