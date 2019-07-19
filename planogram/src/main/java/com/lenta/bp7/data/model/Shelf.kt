package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Shelf(
        val id: Int,
        val number: String,
        val checkTime: Date = Date(),
        var status: ShelfStatus = ShelfStatus.UNFINISHED,
        val goods: MutableList<Good> = mutableListOf()
) {

    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkTime)
    }

    fun getNumberOfGoods(): Int {
        return goods.size
    }

}

enum class ShelfStatus {
    UNFINISHED,
    PROCESSED,
    DELETED
}