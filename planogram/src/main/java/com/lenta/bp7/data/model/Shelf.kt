package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Shelf(
        val id: Int,
        val number: String,
        val checkStart: Date = Date(),
        var checkFinish: Date? = null,
        private var status: ShelfStatus = ShelfStatus.UNFINISHED,
        val goods: MutableList<Good> = mutableListOf()
) {

    fun setStatus(status: ShelfStatus) {
        this.status = status

        if (status != ShelfStatus.UNFINISHED) {
            checkFinish = Date()
        }
    }

    fun getStatus(): ShelfStatus {
        return status
    }

    fun getFormattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(checkStart)
    }

}

enum class ShelfStatus {
    UNFINISHED,
    PROCESSED,
    DELETED
}