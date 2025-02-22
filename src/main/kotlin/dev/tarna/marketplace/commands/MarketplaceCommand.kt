package dev.tarna.marketplace.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.tarna.marketplace.api.commands.BaseCommand
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.marketplace.Economy
import dev.tarna.marketplace.api.marketplace.Marketplace
import dev.tarna.marketplace.api.utils.not
import dev.tarna.marketplace.api.utils.openGUISync
import dev.tarna.marketplace.api.utils.player
import me.tech.mcchestui.GUI
import me.tech.mcchestui.GUIType
import me.tech.mcchestui.item.item
import me.tech.mcchestui.utils.gui
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source

class MarketplaceCommand : BaseCommand() {
    @Command("marketplace")
    @Permission("marketplace.marketplace")
    @CommandDescription("Open the marketplace.")
    suspend fun marketplace(player: PlayerSource) {
        val player = player.source()
        val items = Marketplace.getItems()
        player.openGUISync(itemsGUI(player, items, false))
    }

    private fun itemsGUI(player: Player, items: List<MarketplaceItem>, blackmarket: Boolean, page: Int = 1): GUI {
        return gui(plugin, !"<red><bold>Marketplace", GUIType.Chest(6)) {
            fillBorder {
                item = item(Material.GRAY_STAINED_GLASS_PANE) {
                    name = !""
                }
            }

            val pages = items.chunked(28)
            if (page > pages.size) {
                player.sendMessage(!"<red><bold>There are no items in the marketplace!")
                return@gui
            }
            val currentPage = pages[page - 1]

            for (marketplaceItem in currentPage) {
                nextAvailableSlot {
                    item = item(marketplaceItem.item ?: ItemStack(Material.BARRIER)) {
                        val mpItem = marketplaceItem.item
                        if (mpItem == null) {
                            name = !"<red><bold>Error"
                        } else {
                            val itemLore = mpItem.lore() ?: mutableListOf()
                            val price = if (blackmarket) marketplaceItem.price / 2 else marketplaceItem.price
                            itemLore.addAll(listOf(
                                !"",
                                !"<gray>Price: <green>${price}",
                            ))
                            lore = itemLore

                            onClick {
                                if (it.inventory.firstEmpty() == -1) {
                                    it.sendMessage(!"<red><bold>Your inventory is full!")
                                    return@onClick
                                }
                                it.openGUISync(confirmationGUI(marketplaceItem, blackmarket))
                            }
                        }
                    }
                }
            }

            if (page > 1) {
                slot(45) {
                    item = item(Material.ARROW) {
                        name = !"Previous Page"
                    }

                    onClick {
                        it.openGUISync(itemsGUI(player, items, blackmarket, page - 1))
                    }
                }
            }

            if (page < pages.size) {
                slot(53) {
                    item = item(Material.ARROW) {
                        name = !"Next Page"
                    }

                    onClick {
                        it.openGUISync(itemsGUI(player, items, blackmarket, page + 1))
                    }
                }
            }
        }
    }

    private fun confirmationGUI(marketplaceItem: MarketplaceItem, blackmarket: Boolean): GUI {
        return gui(plugin, !"<red><bold>Confirmation", GUIType.Chest(3)) {
            fillBorder {
                item = item(Material.GRAY_STAINED_GLASS_PANE) {
                    name = !""
                }
            }

            slot(4, 2) {
                item = item(Material.BARRIER) {
                    name = !"<red><bold>Cancel"
                    lore = !listOf(
                        "",
                        "<gray>Click to cancel the transaction"
                    )
                }

                onClick {
                    plugin.launch {
                        it.openGUISync(itemsGUI(it, Marketplace.getItems(blackmarket), blackmarket))
                    }
                }
            }

            slot(6, 2) {
                item = item(Material.EMERALD) {
                    name = !"<green><bold>Confirm"
                    lore = !listOf(
                        "",
                        "<gray>Click to confirm the transaction"
                    )
                }

                onClick {
                    if (Economy.has(it, marketplaceItem.price)) {
                        plugin.launch { Marketplace.buyItem(it, marketplaceItem.seller.player(), marketplaceItem) }
                        it.sendMessage(!"<red><bold>You have successfully bought the item!")
                        it.inventory.close()
                    } else {
                        it.sendMessage(!"<red><bold>You don't have enough money!")
                    }
                }
            }
        }
    }

    @Command("blackmarket")
    @Permission("marketplace.blackmarket")
    @CommandDescription("Open the blackmarket.")
    suspend fun blackmarket(player: PlayerSource) {
        val player = player.source()
        val items = Marketplace.getItems(true)
        player.openGUISync(itemsGUI(player, items, true))
    }

    @Command("blackmarket refresh")
    @Permission("marketplace.blackmarket.refresh")
    @CommandDescription("Refresh the blackmarket.")
    suspend fun refresh(sender: Source) {
        val sender = sender.source()
        Marketplace.refreshBlackmarket()
        sender.sendMessage(!"<red><bold>Blackmarket refreshed!")
    }
}