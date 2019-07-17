package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

class Shelf(
        val id: Int,
        val number: String,
        val checkTime: Date = Date(),
        var status: ShelfStatus = ShelfStatus.CREATED,
        val goods: MutableList<Good> = mutableListOf()
) {

    var currentGoodIndex = 0

    fun getCurrentGood(): Good {
        return goods[currentGoodIndex]
    }

    fun addGood() {
        // todo логика добавления товара в список

        currentGoodIndex = 0
    }

    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkTime)
    }

    fun getNumberOfGoods(): Int {
        return goods.size
    }

}

enum class ShelfStatus {
    CREATED,
    PROCESSED,
    DELETED
}