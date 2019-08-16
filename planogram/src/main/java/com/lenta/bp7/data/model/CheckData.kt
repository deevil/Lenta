package com.lenta.bp7.data.model

import com.lenta.bp7.data.CheckResultData
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.IPersistCheckResult
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.platform.constants.Constants.CHECK_DATA_TIME_FORMAT
import com.lenta.shared.utilities.Logg
import org.simpleframework.xml.core.Persister
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CheckData @Inject constructor(
        private val persistCheckResult: IPersistCheckResult
) {
    val segments: MutableList<Segment> = mutableListOf()

    lateinit var marketNumber: String
    lateinit var checkType: CheckType

    var countFacings = false
    var checkEmptyPlaces = false

    var currentSegmentIndex = 0
    var currentShelfIndex = 0
    var currentGoodIndex = 0

    init {
        val savedResult = persistCheckResult.getSavedCheckResult()
        if (savedResult != null) {
            restoreSavedCheckResult(savedResult)
        }

        //generateTestData()
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
                    ean = goodInfo.ean,
                    material = goodInfo.material,
                    matcode = goodInfo.matcode,
                    enteredCode = goodInfo.enteredCode,
                    name = goodInfo.name,
                    unitsCode = goodInfo.unitsCode,
                    units = goodInfo.units))
        }
        currentGoodIndex = 0
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
        return if (getCurrentShelf()?.goods?.isNotEmpty() == true) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex].let { shelf ->
                    val previousIndex = currentGoodIndex + 1
                    if (previousIndex < shelf.goods.size) shelf.goods[previousIndex] else null
                }
            }
        } else null
    }

    fun getFirstGood(): Good? {
        return if (getCurrentShelf()?.goods?.isNotEmpty() == true) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex].let { shelf ->
                    shelf.goods[0]
                }
            }
        } else null
    }

    fun getSecondGood(): Good? {
        return if (getCurrentShelf()?.goods?.size ?: 0 > 1) {
            segments[currentSegmentIndex].let { segment ->
                segment.shelves[currentShelfIndex].let { shelf ->
                    shelf.goods[1]
                }
            }
        } else null
    }

    fun deleteCurrentSegment() {
        if (segments.isNotEmpty()) {
            segments.removeAt(currentSegmentIndex)
            currentSegmentIndex = 0
        }
    }

    fun deleteCurrentShelf() {
        getCurrentSegment()?.apply {
            if (shelves.isNotEmpty()) {
                shelves.removeAt(currentShelfIndex)
                currentShelfIndex = 0
            }
        }
    }

    fun deleteCurrentGood() {
        getCurrentShelf()?.apply {
            if (goods.isNotEmpty()) {
                goods.removeAt(currentGoodIndex)
                currentGoodIndex = 0
            }
        }
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

    fun setShelfStatusDeletedByIndex(shelfIndex: Int) {
        if (getCurrentSegment()?.shelves?.size ?: 0 > shelfIndex) {
            getCurrentSegment()!!.shelves[shelfIndex].setStatus(ShelfStatus.DELETED)
        }
    }

    fun isExistUnsentData(): Boolean {
        return segments.isNotEmpty()
    }

    fun isExistUnfinishedSegment(): Boolean {
        return segments.find { it.getStatus() == SegmentStatus.UNFINISHED } != null
    }

    fun setUnfinishedSegmentAsCurrent() {
        currentSegmentIndex = segments.indexOf(segments.find { it.getStatus() == SegmentStatus.UNFINISHED })
    }

    fun removeAllFinishedSegments() {
        val unfinishedSegment = segments.find { it.getStatus() == SegmentStatus.UNFINISHED }

        segments.clear()
        if (unfinishedSegment != null) segments.add(unfinishedSegment)
    }

    fun isFirstCurrentGood(): Boolean {
        return getCurrentGood()?.ean == getFirstGood()?.ean
    }

    fun getPreviousSameGoodFacings(): Int {
        return if (getFirstGood()?.ean == getSecondGood()?.ean) getSecondGood()?.facings ?: 0 else 0
    }

    fun removeCurrentGoodIfSamePrevious() {
        if (getCurrentGood()?.ean == getFirstGood()?.ean) {
            val first = getFirstGood()
            val second = getSecondGood()
            if (first != null && second != null) {
                if (first.ean == second.ean && first.getStatus() == second.getStatus()) {
                    getSecondGood()!!.facings += getFirstGood()!!.facings
                    deleteCurrentGood()
                }
            }
        }
    }


    fun getFormattedMarketNumber(): String {
        var number = marketNumber
        while (number.startsWith("0")) {
            number = number.substring(1)
        }
        return number
    }

    fun prepareXmlCheckResult(marketIp: String): String {
        // XML со списком неотправленных сегментов
        val displayOfGoods = DisplayOfGoods(
                marketIp = marketIp
        )

        for (segment in segments.reversed()) {
            if (segment.getStatus() != SegmentStatus.UNFINISHED) {
                val segmentSend = SegmentSend(
                        number = segment.number,
                        startTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(segment.checkStart),
                        completionTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(segment.checkFinish),
                        canceled = if (segment.getStatus() == SegmentStatus.DELETED) 1 else null
                )

                for (shelf in segment.shelves.reversed()) {
                    val shelfSend = ShelfSend(
                            number = shelf.number,
                            startTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(shelf.checkStart),
                            completionTime = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(shelf.checkFinish),
                            counted = if (countFacings) 1 else 0,
                            canceled = if (shelf.getStatus() == ShelfStatus.DELETED) 1 else null
                    )

                    for (good in shelf.goods.reversed()) {
                        val goodSend = GoodSend(
                                sapCodeForSend = good.getFormattedMaterial() + "_${good.unitsCode}",
                                barCode = if (good.enteredCode == EnteredCode.EAN) good.ean
                                        ?: "Not found!" else "",
                                count = if (countFacings) good.facings else null,
                                labeled = if (checkEmptyPlaces && good.facings == 0) {
                                    if (good.getStatus() == GoodStatus.MISSING_WRONG) 0 else 1
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


    fun saveCheckResult() {
        persistCheckResult.saveCheckResult(this)
    }

    fun restoreSavedCheckResult(checkResultData: CheckResultData) {
        checkType = checkResultData.checkType
        countFacings = checkResultData.countFacings
        checkEmptyPlaces = checkResultData.checkEmptyPlaces
        segments.addAll(checkResultData.segments)
    }

    fun clearSavedData() {
        persistCheckResult.clearSavedData()
    }

}