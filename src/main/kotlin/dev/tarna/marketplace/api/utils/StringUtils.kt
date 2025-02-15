package dev.tarna.marketplace.api.utils

import net.kyori.adventure.text.minimessage.MiniMessage

val mm = MiniMessage.miniMessage()

operator fun String.not() = mm.deserialize(this)

operator fun List<String>.not() = map { !it }