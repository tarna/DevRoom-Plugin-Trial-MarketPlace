package dev.tarna.marketplace.api.database.models

import dev.tarna.marketplace.api.utils.deserializeItem
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MarketplaceItem(
    @Contextual
    val id: UUID = UUID.randomUUID(),
    @Contextual
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    var blackmarket: Boolean = false,
) {
    val item get() = deserializeItem(serializedItem)
}