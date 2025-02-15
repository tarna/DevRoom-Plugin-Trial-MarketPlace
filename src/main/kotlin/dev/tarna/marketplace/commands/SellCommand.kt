package dev.tarna.marketplace.commands

import dev.tarna.marketplace.api.commands.BaseCommand
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.utils.serializeItem
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class SellCommand : BaseCommand() {
    @Command("sell")
    @Permission("marketplace.sell")
    @CommandDescription("Sell an item on the marketplace.")
    suspend fun sell(
        player: Player,
        @Argument("price") price: Double
    ) {
        val item = player.inventory.itemInMainHand

        itemCollection.insertOne(
            MarketplaceItem(
                seller = player.uniqueId,
                serializedItem = serializeItem(item),
                price = price
            )
        )

        player.inventory.setItemInMainHand(null)
    }
}