package dev.tarna.marketplace.api.marketplace

import dev.tarna.marketplace.MarketPlacePlugin
import org.bukkit.OfflinePlayer

object Economy {
    private val economy = MarketPlacePlugin.economy

    fun has(player: OfflinePlayer, amount: Double): Boolean {
        return economy.getBalance(player) >= amount
    }

    fun remove(player: OfflinePlayer, amount: Double) {
        economy.withdrawPlayer(player, amount)
    }

    fun add(player: OfflinePlayer, amount: Double) {
        economy.depositPlayer(player, amount)
    }
}