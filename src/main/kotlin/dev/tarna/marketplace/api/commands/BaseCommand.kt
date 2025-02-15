package dev.tarna.marketplace.api.commands

import dev.tarna.marketplace.MarketPlacePlugin

open class BaseCommand {
    val plugin = MarketPlacePlugin.instance
    val itemCollection = MarketPlacePlugin.itemsCollection
    val transactionCollection = MarketPlacePlugin.transactionsCollection
}