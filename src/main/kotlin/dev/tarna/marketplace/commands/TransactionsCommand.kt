package dev.tarna.marketplace.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.tarna.marketplace.api.commands.BaseCommand
import dev.tarna.marketplace.api.database.models.Transaction
import dev.tarna.marketplace.api.marketplace.Transactions
import dev.tarna.marketplace.api.utils.not
import dev.tarna.marketplace.api.utils.player
import me.tech.mcchestui.GUI
import me.tech.mcchestui.GUIType
import me.tech.mcchestui.item.item
import me.tech.mcchestui.utils.gui
import me.tech.mcchestui.utils.openGUI
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class TransactionsCommand : BaseCommand() {
    @Command("transactions")
    @Permission("marketplace.transactions")
    @CommandDescription("View your transactions.")
    fun transactions(player: Player) {
        player.openGUI(mainTransactionsGUI())
    }

    private fun mainTransactionsGUI(): GUI {
        return gui(plugin, !"<red>Transactions", GUIType.Chest(3)) {
            fill(1, 1, 9, 3) {
                item = item(Material.GRAY_STAINED_GLASS_PANE) {
                    name = !" "
                }
            }

            slot(4, 2) {
                item = item(Material.CHEST) {
                    name = !"<red>Things I Sold"
                }

                onClick {
                    plugin.launch {
                        val transactions = Transactions.getSellerTransactions(it)
                        it.openGUI(transactionsGUI(transactions))
                    }
                }
            }

            slot(6, 2) {
                item = item(Material.CHEST) {
                    name = !"<red>Things I Bought"
                }

                onClick {
                    plugin.launch {
                        val transactions = Transactions.getBuyerTransactions(it)
                        it.openGUI(transactionsGUI(transactions))
                    }
                }
            }
        }
    }

    private fun transactionsGUI(transactions: List<Transaction>, page: Int = 1): GUI {
        val pages = transactions.chunked(28)
        val currentPage = pages[page - 1]
        return gui(plugin, !"<red>Transactions", GUIType.Chest(6)) {
            fillBorder {
                item = item(Material.GRAY_STAINED_GLASS_PANE) {
                    name = !" "
                }
            }

            for (transaction in currentPage) {
                nextAvailableSlot {
                    item = item(transaction.item ?: ItemStack(Material.CHEST)) {
                        name = !"<red>Transaction"
                        lore = listOf(
                            !"Seller: ${transaction.seller.player().name}",
                            !"Buyer: ${transaction.buyer.player().name}",
                            !"Price: ${transaction.price}"
                        )
                    }
                }
            }

            if (page > 1) {
                slot(45) {
                    item = item(Material.ARROW) {
                        name = !"Previous Page"
                    }

                    onClick {
                        it.openGUI(transactionsGUI(transactions, page - 1))
                    }
                }
            }

            if (page < pages.size) {
                slot(53) {
                    item = item(Material.ARROW) {
                        name = !"Next Page"
                    }

                    onClick {
                        it.openGUI(transactionsGUI(transactions, page + 1))
                    }
                }
            }
        }
    }
}