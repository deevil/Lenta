package com.lenta.bp7.data.model

import java.util.*

class Shelf(
        val id: Int,
        val number: String,
        val checkTime: Date = Date(),
        var status: ShelfStatus,
        val goods: MutableList<Good> = mutableListOf()
)

enum class ShelfStatus {
    PROCESSED,
    DELETED
}