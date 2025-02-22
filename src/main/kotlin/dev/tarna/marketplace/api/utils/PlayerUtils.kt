package dev.tarna.marketplace.api.utils

import dev.tarna.marketplace.MarketPlacePlugin
import me.tech.mcchestui.GUI
import me.tech.mcchestui.utils.openGUI
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

fun CommandSender.send(vararg messages: String) {
    messages.forEach { sendMessage(!it) }
}

fun UUID.player() = Bukkit.getOfflinePlayer(this)

fun Player.openGUISync(gui: GUI) {
    Bukkit.getScheduler().runTask(MarketPlacePlugin.instance, Runnable {
        openGUI(gui)
    })
}