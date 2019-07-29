package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import java.text.SimpleDateFormat
import java.util.*

data class Shelf(
        val id: Int,
        @Attribute(name = "number")
        val number: String,
        val checkTime: Date = Date(),
        @Attribute(name = "startTime")
        var startTime: String = "",
        @Attribute(name = "completionTime")
        var completionTime: String = "",
        @Attribute(name = "counted")
        val counted: Int,
        @Attribute(name = "canceled")
        var canceled: Int = 0,
        private var status: ShelfStatus = ShelfStatus.UNFINISHED,
        @ElementList(name = "goods")
        val goods: MutableList<Good> = mutableListOf()
) {
    // <shelf number="1" startTime="2019-07-28T01:45:08" completionTime="2019-07-28T01:45:38" counted="0">
    init {
        startTime = checkTime.toString()
    }

    fun setStatus(status: ShelfStatus) {
        this.status = status

        if (status != ShelfStatus.UNFINISHED) {
            completionTime = Date().toString()
        }

        canceled = if (status == ShelfStatus.DELETED) 1 else 0
    }

    fun getStatus(): ShelfStatus {
        return status
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