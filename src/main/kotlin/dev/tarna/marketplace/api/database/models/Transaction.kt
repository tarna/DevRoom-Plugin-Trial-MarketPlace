package dev.tarna.marketplace.api.database.models

import dev.tarna.marketplace.api.utils.deserializeItem
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Transaction(
    @Contextual
    val id: UUID = UUID.randomUUID(),
    @Contextual
    val buyer: UUID,
    @Contextual
    val seller: UUID,
    val serializedItem: String,
    val price: Double,
    val blackmarket: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val item get() = deserializeItem(serializedItem)
}