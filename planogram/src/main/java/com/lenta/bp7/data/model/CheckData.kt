package com.lenta.bp7.data.model

import com.lenta.shared.utilities.Logg

class CheckData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    var checkType: String? = ""
    var currentSegmentIndex = 0

    init {
        generateTestData()
    }

    fun getCurrentSegment(): Segment {
        return segments[currentSegmentIndex]
    }

    fun addSegment(storeNumber: String, segmentNumber: String) {
        segments.add(0, Segment(
                id = segments.lastIndex + 2,
                storeNumber = storeNumber,
                number = segmentNumber))
        currentSegmentIndex = 0
    }

    fun deleteCurrentSegment() {
        segments.removeAt(currentSegmentIndex)
        currentSegmentIndex = 0
    }

    fun isExistUnfinishedSegment(): Boolean {
        return segments.find { it.status == SegmentStatus.UNFINISHED } != null
    }

    private fun generateTestData() {
        Logg.d { "Test data generation for CheckData" }

        for (i in 1..5) {
            segments.add(0, Segment(
                    id = i,
                    number = (100..999).random().toString() + "-" + (100..999).random().toString(),
                    storeNumber = "0007",
                    status = SegmentStatus.PROCESSED,
                    shelves = createShelvesList()))
        }
    }

    private fun createShelvesList(): MutableList<Shelf> {
        val shelves: MutableList<Shelf> = mutableListOf()
        for (i in 1..(3..8).random()) {
            shelves.add(0, Shelf(
                    id = i,
                    number = i.toString(),
                    status = ShelfStatus.PROCESSED,
                    goods = createGoodsList()))
        }

        return shelves
    }

    private fun createGoodsList(): MutableList<Good> {
        val goods: MutableList<Good> = mutableListOf()
        for (i in 1..(10..50).random()) {
            val facings = (0..35).random()
            goods.add(0, Good(
                    id = i,
                    sapCode = createSapCode(),
                    barCode = (10000000000..99999999999).random().toString(),
                    name = "Товар " + (1..1000).random(),
                    status = createGoodStatus(facings),
                    totalFacings = facings))
        }

        return goods
    }

    private fun createSapCode(): String {
        var sap = (1..999999).random().toString()
        while (sap.length < 6) {
            sap = "0$sap"
        }

        return sap
    }

    private fun createGoodStatus(facings: Int): GoodStatus {
        return if (facings == 0) {
            when ((2..3).random()) {
                2 -> GoodStatus.MISSING
                3 -> GoodStatus.PRESENT
                else -> GoodStatus.CREATED
            }
        } else {
            GoodStatus.CREATED
        }
    }
}