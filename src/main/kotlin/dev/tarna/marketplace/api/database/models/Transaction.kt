package dev.tarna.marketplace.api.database.models

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import dev.tarna.marketplace.api.database.serializers.UUIDSerializer
import dev.tarna.marketplace.api.utils.WebhookUtils
import dev.tarna.marketplace.api.utils.deserializeItem
import dev.tarna.marketplace.api.utils.player
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class Transaction(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val buyer: UUID,
    @Serializable(with = UUIDSerializer::class)
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    val blackmarket: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val item get() = deserializeItem(serializedItem)

    fun log() {
        val embed = WebhookEmbedBuilder()
            .setTitle(WebhookEmbed.EmbedTitle("New Transaction", null))
            .setDescription("Transaction ID: $id\nBuyer: ${buyer.player().name} ($buyer) \nSeller: ${seller.player().name} ($seller) \nPrice: $price\nBlackmarket: ${if (blackmarket) "✅" else "❌"}")
            .setTimestamp(Instant.ofEpochMilli(timestamp))
            .setColor(0x00FF00)
            .build()

        WebhookUtils.send(embed)
    }
}