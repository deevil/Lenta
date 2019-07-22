package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Segment(
        val id: Int,
        val storeNumber: String,
        val number: String,
        val checkDate: Date = Date(),
        var status: SegmentStatus = SegmentStatus.UNFINISHED,
        val shelves: MutableList<Shelf> = mutableListOf()
) {

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