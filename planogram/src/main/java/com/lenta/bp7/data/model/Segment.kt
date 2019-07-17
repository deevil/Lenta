package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Segment(
        val id: Int,
        val storeNumber: String,
        val number: String,
        val checkDate: Date = Date(),
        var status: SegmentStatus = SegmentStatus.CREATED,
        val shelves: MutableList<Shelf> = mutableListOf()
) {

    var currentShelfIndex = 0

    fun getCurrentShelf(): Shelf {
        return shelves[currentShelfIndex]
    }

    fun addShelf(shelfNumber: String) {
        shelves.add(0, Shelf(
                id = shelves.lastIndex + 2,
                number = shelfNumber))

        currentShelfIndex = 0
    }

    fun getFormattedDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(checkDate)
    }

    fun getNumberOfShelves(): Int {
        return shelves.size
    }

}

enum class SegmentStatus {
    CREATED,
    UNFINISHED,
    PROCESSED,
    DELETED
}