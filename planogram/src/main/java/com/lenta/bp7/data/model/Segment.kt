package com.lenta.bp7.data.model

import java.util.*

class Segment(
        val id: Int,
        val number: String,
        val checkDate: Date = Date(),
        var status: SegmentStatus = SegmentStatus.STARTED,
        val shelves: MutableList<Shelf> = mutableListOf()
)

enum class SegmentStatus {
    STARTED,
    UNFINISHED,
    PROCESSED,
    DELETED
}