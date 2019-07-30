package com.lenta.bp7.data.model

import com.lenta.bp7.data.CheckType
import com.lenta.shared.platform.constants.Constants.CHECK_DATA_TIME_FORMAT
import com.lenta.shared.utilities.Logg
import org.simpleframework.xml.core.Persister
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


class CheckData(
        val segments: MutableList<Segment> = mutableListOf()
) {

    var checkType: CheckType = CheckType.SELF_CONTROL
    var countFacings = true
    var checkEmptyPlaces = true

    var currentSegmentIndex = 0
    var currentShelfIndex = 0
    var currentGoodIndex = 0

    init {
        generateTestData()
    }

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
                    number = shelfNumber))
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

    fun prepareDataForSend(): String? {
        // Будущий XML со списком неотправленных сегментов
        val displayOfGoods = DisplayOfGoods()
        for (segment in segments) {
            if (segment.getStatus() != SegmentStatus.UNFINISHED) {
                val segmentSend = SegmentSend(
                        number = segment.number,
                        startTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(segment.checkStart),
                        completionTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(segment.checkFinish),
                        canceled = if (segment.getStatus() == SegmentStatus.DELETED) 1 else 0
                )

                for (shelf in segment.shelves) {
                    val shelfSend = ShelfSend(
                            number = shelf.number,
                            startTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(shelf.checkStart),
                            completionTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(shelf.checkFinish),
                            counted = if (countFacings) 1 else 0,
                            canceled = if (shelf.getStatus() == ShelfStatus.DELETED) 1 else 0
                    )

                    for (good in shelf.goods) {
                        val goodSend = GoodSend(
                                sapCodeForSend = good.getFormattedSapCode() + "_${good.unitsCode}",
                                barCode = good.barCode,
                                count = if (countFacings) good.facings else null,
                                labeled = if (checkEmptyPlaces) {
                                    when (good.getStatus()) {
                                        GoodStatus.MISSING_WRONG -> 0
                                        else -> 1
                                    }
                                } else null
                        )

                        shelfSend.goods.add(goodSend)
                    }

                    segmentSend.shelves.add(shelfSend)
                }

                displayOfGoods.segments.add(segmentSend)
            }
        }

        val serializer = Persister()
        val result = StringWriter()
        serializer.write(displayOfGoods, result)

        Logg.d { "displayOfGoods --> $result" }

        return result.toString()
    }


    private fun generateTestData() {
        Logg.d { "Test data generation for CheckData" }
        segments.add(0, Segment(
                id = 0,
                number = (100..999).random().toString() + "-" + (100..999).random().toString(),
                storeNumber = "0007",
                checkFinish = Date(),
                status = SegmentStatus.PROCESSED,
                shelves = createShelvesList()))

    }

    private fun createShelvesList(): MutableList<Shelf> {
        val shelves: MutableList<Shelf> = mutableListOf()
        shelves.add(0, Shelf(
                id = 0,
                checkFinish = Date(),
                number = "12",
                status = ShelfStatus.PROCESSED,
                goods = createGoodsList()))

        return shelves
    }

    private fun createGoodsList(): MutableList<Good> {
        val goods: MutableList<Good> = mutableListOf()
        goods.add(0, Good(
                id = 0,
                sapCode = "000000000000" + (100000..999999).random().toString(),
                barCode = (100000000000..999999999999).random().toString(),
                name = "Товар " + (1..1000).random(),
                status = GoodStatus.PROCESSED,
                facings = 15,
                unitsCode = "ST",
                units = "шт"))

        return goods
    }
}