package dev.tarna.marketplace.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.tarna.marketplace.api.commands.BaseCommand
import dev.tarna.marketplace.api.database.models.MarketplaceItem
import dev.tarna.marketplace.api.marketplace.Economy
import dev.tarna.marketplace.api.marketplace.Marketplace
import dev.tarna.marketplace.api.utils.not
import dev.tarna.marketplace.api.utils.player
import me.tech.mcchestui.GUI
import me.tech.mcchestui.GUIType
import me.tech.mcchestui.item.item
import me.tech.mcchestui.utils.gui
import me.tech.mcchestui.utils.openGUI
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class MarketplaceCommand : BaseCommand() {
    @Command("marketplace")
    @Permission("marketplace.marketplace")
    @CommandDescription("Open the marketplace.")
    suspend fun marketplace(player: Player) {
        val items = Marketplace.getItems()
        player.openGUI(itemsGUI(items, false))
    }

    private fun itemsGUI(items: List<MarketplaceItem>, blackmarket: Boolean, page: Int = 1): GUI {
        return gui(plugin, !"<red><bold>Marketplace", GUIType.Chest(6)) {
            fillBorder {
                item = item(Material.GRAY_STAINED_GLASS_PANE) {
                    name = !""
                }
            }

            val pages = items.chunked(28)
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
                                it.openGUI(confirmationGUI(marketplaceItem, blackmarket))
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
                        it.openGUI(itemsGUI(items, blackmarket, page - 1))
                    }
                }
            }

            if (page < pages.size) {
                slot(53) {
                    item = item(Material.ARROW) {
                        name = !"Next Page"
                    }

                    onClick {
                        it.openGUI(itemsGUI(items, blackmarket, page + 1))
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
                        it.openGUI(itemsGUI(Marketplace.getItems(blackmarket), blackmarket))
                    }
                }
            }

            slot(4, 6) {
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
    suspend fun blackmarket(player: Player) {
        val items = Marketplace.getItems(true)
        player.openGUI(itemsGUI(items, true))
    }

    @Command("blackmarket refresh")
    @Permission("marketplace.blackmarket.refresh")
    @CommandDescription("Refresh the blackmarket.")
    suspend fun refresh(sender: CommandSender) {
        Marketplace.refreshBlackmarket()
        sender.sendMessage(!"<red><bold>Blackmarket refreshed!")
    }
}