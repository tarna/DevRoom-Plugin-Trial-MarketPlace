package dev.tarna.marketplace.api.marketplace

import com.mongodb.client.model.Filters.eq
import dev.tarna.marketplace.MarketPlacePlugin
import dev.tarna.marketplace.api.database.models.Transaction
import kotlinx.coroutines.flow.toList
import org.bukkit.entity.Player

object Transactions {
    private val transactionsCollection = MarketPlacePlugin.transactionsCollection

    suspend fun getBuyerTransactions(player: Player): List<Transaction> {
        return transactionsCollection.find(
            eq("buyer", player.uniqueId)
        ).toList()
    }

    suspend fun getSellerTransactions(player: Player): List<Transaction> {
        return transactionsCollection.find(
            eq("seller", player.uniqueId)
        ).toList()
    }
}