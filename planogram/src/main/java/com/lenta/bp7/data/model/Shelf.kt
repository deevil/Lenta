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

    var currentGoodIndex = 0

    fun getCurrentGood(): Good {
        return goods[currentGoodIndex]
    }

    fun addGood(sapCode: String?, barCode: String?, name: String?, units: String?) {
        goods.add(0, Good(
                id = goods.lastIndex + 2,
                sapCode = sapCode,
                barCode = barCode,
                name = name,
                units = units))

        currentGoodIndex = 0
    }

    fun addGoodByBarCode(barCode: String) {
        goods.add(0, Good(
                id = goods.lastIndex + 2,
                barCode = barCode))

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
    UNFINISHED,
    PROCESSED,
    DELETED
}