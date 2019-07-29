package com.lenta.bp7.data.model

import java.util.*


data class DisplayOfGoods(
        val documentDate: String,
        val id: String,
        val gid: String = UUID.randomUUID().toString(),
        val equipment: Array<Equipment>
)

data class Equipment(
        val number: String,
        val startTime: String,
        val completionTime: String,
        val canceled: String,
        val shelf: Array<SendShelf>
)

data class SendShelf(
        val number: String,
        val counted: String,
        val startTime: String,
        val completionTime: String,
        val canceled: String,
        val goods: Array<SendGood>
)

data class SendGood(
        val SAPCode: String,
        val barcode: String,
        val count: String,
        val labeled: String
)
