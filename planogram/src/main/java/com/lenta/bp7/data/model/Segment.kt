package com.lenta.bp7.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Segment(
        val id: Int,
        val storeNumber: String,
        val number: String,
        val checkStart: Date = Date(),
        var checkFinish: Date? = null,
        private var status: SegmentStatus = SegmentStatus.UNFINISHED,
        val shelves: MutableList<Shelf> = mutableListOf()
) {

    fun setStatus(status: SegmentStatus) {
        this.status = status

        if (status == SegmentStatus.PROCESSED || status == SegmentStatus.DELETED) {
            checkFinish = Date()
        }
    }

    fun getStatus(): SegmentStatus {
        return status
    }

    fun getFormattedDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(checkStart)
    }

}

enum class SegmentStatus {
    UNFINISHED,
    PROCESSED,
    DELETED
}