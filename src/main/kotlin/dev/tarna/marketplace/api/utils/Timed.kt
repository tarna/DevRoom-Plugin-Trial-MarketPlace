package dev.tarna.marketplace.api.utils

import dev.tarna.marketplace.MarketPlacePlugin
import kotlin.system.measureTimeMillis

inline fun timedEnabled(name: String, block: () -> Boolean): Boolean {
    val time = measureTimeMillis {
        if (!block()) return false
    }
    MarketPlacePlugin.instance.logger.info("$name enabled in ${time}ms")
    return true
}

inline fun timedDisabled(name: String, block: () -> Boolean): Boolean {
    val time = measureTimeMillis {
        if (!block()) return false
    }
    MarketPlacePlugin.instance.logger.info("$name disabled in ${time}ms")
    return true
}