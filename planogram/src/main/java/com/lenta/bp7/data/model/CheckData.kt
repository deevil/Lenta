package com.lenta.bp7.data.model

import com.lenta.bp7.data.CheckType

class CheckData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    var checkType: CheckType = CheckType.SELF_CONTROL
    var countFacings = false
    var checkEmptyPlaces = false

    var currentSegmentIndex = 0
    var currentShelfIndex = 0
    var currentGoodIndex = 0


    fun getCurrentSegment(): Segment? {
        return if (segments.isNotEmpty()) {
            segments[currentSegmentIndex]
        } else null
    }

    fun getCurrentShelf(): Shelf? {
        return if (getCurrentSegment()?.shelves?.isNotEmpty() == true) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex]
            }
        } else null

    }

    fun getCurrentGood(): Good? {
        return if (getCurrentShelf()?.goods?.isNotEmpty() == true) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex].let { shelf ->
                    shelf.goods[currentGoodIndex]
                }
            }
        } else null
    }

    fun getPreviousGood(): Good? {
        return if (getCurrentShelf()?.goods?.size ?: 0 > 1) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex].let { shelf ->
                    shelf.goods[currentGoodIndex + 1]
                }
            }
        } else null
    }

    fun addSegment(storeNumber: String, segmentNumber: String) {
        segments.add(0, Segment(
                id = segments.lastIndex + 2,
                storeNumber = storeNumber,
                number = segmentNumber))
        currentSegmentIndex = 0
    }

    fun addShelf(shelfNumber: String) {
        getCurrentSegment()!!.shelves.let {
            it.add(0, Shelf(
                    id = it.lastIndex + 2,
                    number = shelfNumber,
                    counted = if (countFacings) 1 else 0))
        }
        currentShelfIndex = 0
    }

    fun addGood(goodInfo: GoodInfo) {
        getCurrentShelf()!!.goods.let {
            it.add(0, Good(
                    id = it.lastIndex + 2,
                    sapCode = goodInfo.sapCode,
                    barCode = goodInfo.barCode,
                    name = goodInfo.name,
                    unitsCode = goodInfo.unitsCode,
                    units = goodInfo.units))
        }
        currentGoodIndex = 0
    }

    fun deleteCurrentSegment() {
        segments.removeAt(currentSegmentIndex)
        currentSegmentIndex = 0
    }

    fun deleteCurrentShelf() {
        getCurrentSegment()?.shelves?.removeAt(currentShelfIndex)
        currentShelfIndex = 0
    }

    fun deleteCurrentGood() {
        getCurrentShelf()?.goods?.removeAt(currentGoodIndex)
        currentGoodIndex = 0
    }

    fun setCurrentSegmentStatus(status: SegmentStatus) {
        getCurrentSegment()?.setStatus(status)
    }

    fun setCurrentShelfStatus(status: ShelfStatus) {
        getCurrentShelf()?.setStatus(status)
    }

    fun setCurrentGoodStatus(status: GoodStatus) {
        getCurrentGood()?.setStatus(status)
        removeCurrentGoodIfSamePrevious()
    }

    private fun removeCurrentGoodIfSamePrevious() {
        val current = getCurrentGood()
        val previous = getPreviousGood()
        if (current != null && previous != null) {
            if (current.barCode == previous.barCode && current.getStatus() == previous.getStatus()) {
                getPreviousGood()!!.facings += getCurrentGood()!!.facings
                deleteCurrentGood()
            }
        }
    }

    fun setShelfStatusDeletedByIndex(shelfIndex: Int) {
        if (getCurrentSegment()?.shelves?.size ?: 0 >= shelfIndex) {
            getCurrentSegment()!!.shelves[shelfIndex].setStatus(ShelfStatus.DELETED)
        }
    }

    fun isExistUnfinishedSegment(): Boolean {
        return segments.find { it.getStatus() == SegmentStatus.UNFINISHED } != null
    }

    fun setUnfinishedSegmentAsCurrent() {
        currentSegmentIndex = segments.indexOf(segments.find { it.getStatus() == SegmentStatus.UNFINISHED })
    }

    fun getPreviousGoodFacings(): Int {
        return if (getCurrentGood()?.barCode == getPreviousGood()?.barCode) getPreviousGood()?.facings
                ?: 0 else 0
    }

    fun prepareDataForSend(): Any? {


        return null
    }

}