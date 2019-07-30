package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Shelf(
        val id: Int,
        val number: String,
        val startTime: Date = Date(),
        var completionTime: Date? = null,
        private var status: ShelfStatus = ShelfStatus.UNFINISHED,
        val goods: MutableList<Good> = mutableListOf()
) {

    fun setStatus(status: ShelfStatus) {
        this.status = status

        if (status != ShelfStatus.UNFINISHED) {
            completionTime = Date()
        }
    }

    fun getStatus(): ShelfStatus {
        return status
    }

    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime)
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