package dev.tarna.marketplace.api.database.models

import dev.tarna.marketplace.api.database.serializers.UUIDSerializer
import dev.tarna.marketplace.api.utils.deserializeItem
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MarketplaceItem(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    var blackmarket: Boolean = false,
) {
    val item get() = deserializeItem(serializedItem)
}