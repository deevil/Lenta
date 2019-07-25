package com.lenta.bp7.data.model

import com.lenta.shared.utilities.Logg

class CheckData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    var checkType = ""
    var countFacings = false
    var checkEmptyPlaces = false

    var currentSegmentIndex = 0
    var currentShelfIndex = 0
    var currentGoodIndex = 0

    init {
        generateTestData()
    }

    fun getCurrentSegment(): Segment {
        return segments[currentSegmentIndex]
    }

    fun getCurrentShelf(): Shelf {
        return segments[currentSegmentIndex].let { segment ->
            segment.shelves[currentShelfIndex]
        }
    }

    fun getCurrentGood(): Good {
        return segments[currentSegmentIndex].let { segment ->
            segment.shelves[currentShelfIndex].let { shelf ->
                shelf.goods[currentGoodIndex]
            }
        }
    }

    fun getLastGood(): Good {
        return segments[currentSegmentIndex].let { segment ->
            segment.shelves[currentShelfIndex].let { shelf ->
                shelf.goods[currentGoodIndex + 1]
            }
        }
    }

    fun addSegment(storeNumber: String, segmentNumber: String) {
        segments.add(0, Segment(
                id = segments.lastIndex + 2,
                storeNumber = storeNumber,
                number = segmentNumber))
        currentSegmentIndex = 0
    }

    fun addShelf(shelfNumber: String) {
        getCurrentSegment().shelves.let {
            it.add(0, Shelf(
                    id = it.lastIndex + 2,
                    number = shelfNumber))
        }
        currentShelfIndex = 0
    }

    fun addGood(goodInfo: GoodInfo?) {
        getCurrentShelf().goods.let {
            it.add(0, Good(
                    id = it.lastIndex + 2,
                    sapCode = goodInfo?.sapCode,
                    barCode = goodInfo?.barCode,
                    name = goodInfo?.name,
                    units = goodInfo?.units))
        }
        currentGoodIndex = 0
    }

    fun deleteCurrentSegment() {
        segments.removeAt(currentSegmentIndex)
        currentSegmentIndex = 0
    }

    fun deleteCurrentShelf() {
        getCurrentSegment().shelves.removeAt(currentShelfIndex)
        currentShelfIndex = 0
    }

    fun deleteCurrentGood() {
        getCurrentShelf().goods.removeAt(currentGoodIndex)
        currentGoodIndex = 0
    }

    fun setCurrentGoodStatus(status: GoodStatus) {
        getCurrentGood().status = status
        removeCurrentGoodIfSameLast()
    }

    fun currentGoodIsSameLast(): Boolean {
        val current = getCurrentGood()
        val last = getLastGood()
        return current.barCode == last.barCode && current.status == last.status
    }

    fun removeCurrentGoodIfSameLast() {
        if (goodsMoreThanOne() && currentGoodIsSameLast()) {
            getLastGood().facings += getCurrentGood().facings
            deleteCurrentGood()
        }
    }

    fun setShelfStatusDeletedByIndex(shelfIndex: Int) {
        getCurrentSegment().shelves[shelfIndex].status = ShelfStatus.DELETED
    }

    fun isExistUnfinishedSegment(): Boolean {
        return segments.find { it.status == SegmentStatus.UNFINISHED } != null
    }

    fun setUnfinishedSegmentAsCurrent() {
        currentSegmentIndex = segments.indexOf(segments.find { it.status == SegmentStatus.UNFINISHED })
    }

    fun getLastGoodFacings(): Int {
        return if (goodsMoreThanOne() && getCurrentGood().barCode == getLastGood().barCode) getLastGood().facings else 0
    }

    private fun goodsMoreThanOne(): Boolean {
        return getCurrentShelf().goods.size > 1
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
                    sapCode = "000000000000" + (100000..999999).random().toString(),
                    barCode = (100000000000..999999999999).random().toString(),
                    name = "Товар " + (1..1000).random(),
                    status = createGoodStatus(facings),
                    facings = facings))
        }

        return goods
    }

    private fun createGoodStatus(facings: Int): GoodStatus {
        return if (facings == 0) {
            when ((2..3).random()) {
                2 -> GoodStatus.MISSING_WRONG
                3 -> GoodStatus.MISSING_RIGHT
                else -> GoodStatus.PROCESSED
            }
        } else {
            GoodStatus.PROCESSED
        }
    }
}