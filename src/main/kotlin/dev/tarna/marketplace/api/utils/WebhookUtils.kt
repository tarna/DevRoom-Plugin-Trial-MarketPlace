package dev.tarna.marketplace.api.utils

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import dev.tarna.marketplace.MarketPlacePlugin

object WebhookUtils {
    fun send(title: String? = null, vararg messages: String) {
        val embed = WebhookEmbedBuilder()
            .setDescription(messages.joinToString("\n"))
            .setColor(0x00FF00)

        if (title != null) embed.setTitle(WebhookEmbed.EmbedTitle(title, null))

        MarketPlacePlugin.webhookClient.send(embed.build())
    }

    fun send(embed: WebhookEmbed) {
        MarketPlacePlugin.webhookClient.send(embed)
    }
}