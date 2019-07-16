package com.lenta.bp7.data.model

import com.lenta.shared.utilities.Logg

class CheckStoreData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    var checkType: String? = ""

    var currentSegment: Segment
    var currentShelf: Shelf

    init {
        generateTestData()

        currentSegment = segments[0]
        currentShelf = segments[0].shelves[0]
    }

    fun addSegment(storeNumber: String?, segmentNumber: String?) {
        currentSegment = Segment(
                id = segments.lastIndex + 1,
                storeNumber = storeNumber,
                number = segmentNumber)

        segments.add(0, currentSegment)
    }

    private fun generateTestData() {
        Logg.d { "Test data generation for CheckStoreData" }

        for (i in 1..5) {
            segments.add(Segment(
                    id = i,
                    number = (100..999).random().toString() + "-" + (100..999).random().toString(),
                    storeNumber = "0007",
                    status = when ((2..4).random()) {
                        2 -> SegmentStatus.UNFINISHED
                        3 -> SegmentStatus.PROCESSED
                        4 -> SegmentStatus.DELETED
                        else -> SegmentStatus.CREATED
                    },
                    shelves = createShelvesList()))
        }
    }

    private fun createShelvesList(): MutableList<Shelf> {
        val shelves: MutableList<Shelf> = mutableListOf()
        for (i in 1..(3..8).random()) {
            shelves.add(Shelf(
                    id = i,
                    number = i.toString(),
                    status = when ((2..3).random()) {
                        2 -> ShelfStatus.PROCESSED
                        3 -> ShelfStatus.DELETED
                        else -> ShelfStatus.CREATED
                    },
                    goods = createGoodsList()))
        }

        return shelves
    }

    private fun createGoodsList(): MutableList<Good> {
        val goods: MutableList<Good> = mutableListOf()
        for (i in 1..(10..50).random()) {
            val facings = (0..35).random()
            goods.add(Good(
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