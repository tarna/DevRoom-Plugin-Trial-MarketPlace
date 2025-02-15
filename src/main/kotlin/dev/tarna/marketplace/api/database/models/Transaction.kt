package dev.tarna.marketplace.api.database.models

import dev.tarna.marketplace.api.utils.deserializeItem
import java.util.UUID

data class Transaction(
    val id: UUID,
    val buyer: UUID,
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    val blackmarket: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val item get() = deserializeItem(serializedItem)
}