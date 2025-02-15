package dev.tarna.marketplace.api.database.models

import dev.tarna.marketplace.api.utils.deserializeItem
import java.util.UUID

data class MarketplaceItem(
    val id: UUID = UUID.randomUUID(),
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    var blackmarket: Boolean = false,
) {
    val item get() = deserializeItem(serializedItem)
}