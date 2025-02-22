package dev.tarna.marketplace.commands

import dev.tarna.marketplace.api.commands.BaseCommand
import dev.tarna.marketplace.api.marketplace.Marketplace
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.paper.util.sender.PlayerSource

class SellCommand : BaseCommand() {
    @Command("sell <price>")
    @Permission("marketplace.sell")
    @CommandDescription("Sell an item on the marketplace.")
    suspend fun sell(
        player: PlayerSource,
        @Argument("price") price: Double
    ) {
        val player = player.source()

        Marketplace.sell(player, price)
    }
}