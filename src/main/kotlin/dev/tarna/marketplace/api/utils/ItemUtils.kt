package dev.tarna.marketplace.api.utils

import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.Base64

fun serializeItem(item: ItemStack?): String {
    return try {
        ByteArrayOutputStream().use { outputStream ->
            DataOutputStream(outputStream).use { output ->
                if (item == null) {
                    output.writeInt(0)
                } else {
                    val bytes = item.serializeAsBytes()
                    output.writeInt(bytes.size)
                    output.write(bytes)
                }
                Base64.getEncoder().encodeToString(outputStream.toByteArray())
            }
        }
    } catch (e: IOException) {
        throw RuntimeException("Error while writing itemstack", e)
    }
}

fun deserializeItem(encodedItem: String): ItemStack? {
    val bytes = Base64.getDecoder().decode(encodedItem)
    return try {
        ByteArrayInputStream(bytes).use { inputStream ->
            DataInputStream(inputStream).use { input ->
                val length = input.readInt()
                if (length == 0) {
                    null
                } else {
                    val itemBytes = ByteArray(length)
                    input.readFully(itemBytes)
                    ItemStack.deserializeBytes(itemBytes)
                }
            }
        }
    } catch (e: IOException) {
        throw RuntimeException("Error while reading itemstack", e)
    }
}