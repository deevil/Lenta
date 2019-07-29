package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import java.text.SimpleDateFormat
import java.util.*

data class Segment(
        val id: Int,
        val storeNumber: String,
        @Attribute(name = "number")
        val number: String,
        val checkDate: Date = Date(),
        @Attribute(name = "startTime")
        var startTime: String = "",
        @Attribute(name = "completionTime")
        var completionTime: String  = "",
        @Attribute(name = "canceled")
        var canceled: Int = 0,
        private var status: SegmentStatus = SegmentStatus.UNFINISHED,
        @ElementList(name = "shelf")
        val shelves: MutableList<Shelf> = mutableListOf()
) {
    // <equipment number="032 007" startTime="2019-07-28T01:45:03" completionTime="2019-07-28T01:48:13">
    init {
        startTime = checkDate.toString()
    }

    fun setStatus(status: SegmentStatus) {
        this.status = status

        if (status == SegmentStatus.PROCESSED || status == SegmentStatus.DELETED) {
            completionTime = Date().toString()
        }

        canceled = if (status == SegmentStatus.DELETED) 1 else 0
    }

    fun getStatus(): SegmentStatus {
        return status
    }

    fun getFormattedDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(checkDate)
    }

    fun getNumberOfShelves(): Int {
        return shelves.size
    }

}

enum class SegmentStatus {
    UNFINISHED,
    PROCESSED,
    DELETED
}