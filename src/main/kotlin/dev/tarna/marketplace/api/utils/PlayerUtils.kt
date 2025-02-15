package dev.tarna.marketplace.api.utils

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.UUID

fun CommandSender.send(vararg messages: String) {
    messages.forEach { sendMessage(!it) }
}

fun UUID.player() = Bukkit.getOfflinePlayer(this)