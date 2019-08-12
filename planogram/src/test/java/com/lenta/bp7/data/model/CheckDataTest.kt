package com.lenta.bp7.data.model

import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.IPersistCheckResult
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

internal class CheckDataTest {

    var checkData: CheckData? = null

    private val marketNumber = "0001"
    private val checkType = CheckType.SELF_CONTROL

    private val customSegmentNumber = "XXX-XXX"
    private val customShelfNumber = "XXX"
    private val customGoodEan = "XXXXXXXX"

    @BeforeEach
    fun createCheckData() {
        val persistCheckResult: IPersistCheckResult = mock()
        checkData = CheckData(persistCheckResult)
        checkData?.let {
            it.marketNumber = marketNumber
            it.checkType = checkType
        }
    }

    @AfterEach
    fun deleteCheckData() {
        checkData = null
    }

    private fun addSegment(
            number: String = "" + (100..999).random() + "-" + (100..999).random(),
            status: SegmentStatus = SegmentStatus.UNFINISHED) {
        checkData?.addSegment(marketNumber, number)
        checkData?.getCurrentSegment()?.setStatus(status)
    }

    private fun addShelf(
            number: String = "" + (1..999).random(),
            status: ShelfStatus = ShelfStatus.UNFINISHED) {
        if (checkData?.getCurrentSegment() == null) addSegment()
        checkData?.addShelf(number)
        checkData?.getCurrentShelf()?.setStatus(status)
    }

    private fun addGood(
            ean: String = "" + (10000000..99999999999999).random(),
            material: String = "000000000000" + (100000..999999).random(),
            matcode: String = "" + (100000000000..999999999999).random(),
            enteredCode: EnteredCode = EnteredCode.EAN,
            name: String = "Good " + (1..999).random(),
            facings: Int = (1..99).random(),
            unitsCode: String = "ST",
            units: String = "шт",
            status: GoodStatus = GoodStatus.CREATED) {
        if (checkData?.getCurrentSegment() == null) addSegment()
        if (checkData?.getCurrentShelf() == null) addShelf()
        checkData?.addGood(GoodInfo(ean, material, matcode, enteredCode, name, unitsCode, units))
        checkData?.getCurrentGood()?.facings = facings
        checkData?.getCurrentGood()?.setStatus(status)
    }

    @Test
    fun `CheckData creation`() {
        assertAll("checkData",
                Executable { assertTrue(checkData?.segments != null) },
                Executable { assertTrue(checkData?.segments?.isEmpty() == true) },
                Executable { assertEquals(marketNumber, checkData?.marketNumber) },
                Executable { assertEquals(checkType, checkData?.checkType) },
                Executable { assertEquals(false, checkData?.countFacings) },
                Executable { assertEquals(false, checkData?.checkEmptyPlaces) },
                Executable { assertEquals(false, checkData?.checkEmptyPlaces) },
                Executable { assertEquals(0, checkData?.currentSegmentIndex) },
                Executable { assertEquals(0, checkData?.currentShelfIndex) },
                Executable { assertEquals(0, checkData?.currentGoodIndex) }
        )
    }

    @Test
    fun `Add segments`() {
        for (i in 1..3) addSegment()
        assertEquals(3, checkData?.segments?.size)
    }

    @Test
    fun `Add shelves`() {
        for (i in 1..4) addShelf()
        assertEquals(4, checkData?.getCurrentSegment()?.shelves?.size)
    }

    @Test
    fun `Add goods`() {
        for (i in 1..5) addGood()
        assertEquals(5, checkData?.getCurrentShelf()?.goods?.size)
    }

    @Test
    fun `Get current segment with empty segment-list`() {
        assertEquals(null, checkData?.getCurrentSegment())
    }

    @Test
    fun `Get current (last added) segment`() {
        for (i in 1..3) addSegment()
        addSegment(customSegmentNumber)
        assertEquals(customSegmentNumber, checkData?.getCurrentSegment()?.number)
    }

    @Test
    fun `Get changed current segment`() {
        addSegment()
        addSegment(customSegmentNumber)
        addSegment() // Текущий сегмент
        checkData?.currentSegmentIndex = 1 // Меняем текущий сегмент
        assertEquals(customSegmentNumber, checkData?.getCurrentSegment()?.number)
    }

    @Test
    fun `Get current shelf with empty shelf-list`() {
        assertEquals(null, checkData?.getCurrentShelf())
    }

    @Test
    fun `Get current (last added) shelf`() {
        addSegment()
        for (i in 1..3) addShelf()
        addShelf(customShelfNumber) // Текущая полка
        assertEquals(customShelfNumber, checkData?.getCurrentShelf()?.number)
    }

    @Test
    fun `Get changed current shelf`() {
        addShelf()
        addShelf(customShelfNumber)
        addShelf() // Текущая полка
        checkData?.currentShelfIndex = 1 // Меняем текущую полку
        assertEquals(customShelfNumber, checkData?.getCurrentShelf()?.number)
    }

    @Test
    fun `Get current good with empty shelf-list`() {
        assertEquals(null, checkData?.getCurrentGood())
    }

    @Test
    fun `Get current (last added) good`() {
        for (i in 1..3) addGood()
        addGood(ean = customGoodEan) // Текущий товар
        assertEquals(customGoodEan, checkData?.getCurrentGood()?.ean)
    }

    @Test
    fun `Get changed current good`() {
        addGood()
        addGood(ean = customGoodEan)
        addGood() // Текущий товар
        checkData?.currentGoodIndex = 1 // Меняем текущий товар
        assertEquals(customGoodEan, checkData?.getCurrentGood()?.ean)
    }

    @Test
    fun `Get previous good from empty good-list`() {
        assertEquals(null, checkData?.getPreviousGood())
    }

    @Test
    fun `Get previous good with first current element`() {
        addGood(ean = customGoodEan) // Предыдущий товар
        addGood() // Текущий товар
        assertEquals(customGoodEan, checkData?.getPreviousGood()?.ean)
    }

    @Test
    fun `Get previous good with changed current element`() {
        addGood(ean = customGoodEan)
        addGood() // Предыдущий товар
        addGood() // Текущий товар
        checkData?.currentGoodIndex = 1 // Меняем текущий товар
        assertEquals(customGoodEan, checkData?.getPreviousGood()?.ean)
    }

    @Test
    fun `Get previous good from last element in list`() {
        val listSize = 5
        for (i in 1..listSize) addGood()
        checkData?.currentGoodIndex = listSize - 1 // Делаем последний элемент текущим
        assertEquals(null, checkData?.getPreviousGood())
    }

    @Test
    fun `Get first good from empty list`() {
        assertEquals(null, checkData?.getFirstGood()?.ean)
    }

    @Test
    fun `Get first good from list`() {
        addGood() // Второй товар в списке
        addGood(ean = customGoodEan) // Первый товар в списке
        assertEquals(customGoodEan, checkData?.getFirstGood()?.ean)
    }

    @Test
    fun `Get second good from empty list`() {
        assertEquals(null, checkData?.getSecondGood()?.ean)
    }

    @Test
    fun `Get second good from list`() {
        addGood(ean = customGoodEan) // Второй товар в списке
        addGood() // Первый товар в списке
        assertEquals(customGoodEan, checkData?.getSecondGood()?.ean)
    }

    @Test
    fun `Delete current segment from empty list`() {
        assertDoesNotThrow { checkData?.deleteCurrentSegment() }
    }

    @Test
    fun `Delete current shelf from empty list`() {
        assertDoesNotThrow { checkData?.deleteCurrentShelf() }
    }

    @Test
    fun `Delete current good from empty list`() {
        assertDoesNotThrow { checkData?.deleteCurrentGood() }
    }

    @Test
    fun `Delete current segment`() {
        addSegment(number = customSegmentNumber)
        addSegment() // Текущий сегмент
        checkData?.currentSegmentIndex = 1 // Меняем текущий сегмент
        checkData?.deleteCurrentSegment()
        assertEquals(null, checkData?.segments?.find { it.number == customSegmentNumber })
        assertEquals(0, checkData?.currentSegmentIndex)
    }

    @Test
    fun `Delete current shelf`() {
        addShelf(number = customShelfNumber)
        addShelf() // Текущая полка
        checkData?.currentShelfIndex = 1 // Меняем текущую полку
        checkData?.deleteCurrentShelf()
        assertEquals(null, checkData?.getCurrentSegment()?.shelves?.find { it.number == customShelfNumber })
        assertEquals(0, checkData?.currentShelfIndex)
    }

    @Test
    fun `Delete current good`() {
        addGood(ean = customGoodEan)
        addGood() // Текущий товар
        checkData?.currentGoodIndex = 1 // Меняем текущий товар
        checkData?.deleteCurrentGood()
        assertEquals(null, checkData?.getCurrentShelf()?.goods?.find { it.ean == customGoodEan })
        assertEquals(0, checkData?.currentGoodIndex)
    }

    @Test
    fun `Change current segment status`() {
        addSegment(status = SegmentStatus.PROCESSED)
        assertEquals(SegmentStatus.PROCESSED, checkData?.getCurrentSegment()?.getStatus())
    }

    @Test
    fun `Change current shelf status`() {
        addShelf(status = ShelfStatus.PROCESSED)
        assertEquals(ShelfStatus.PROCESSED, checkData?.getCurrentShelf()?.getStatus())
    }

    @Test
    fun `Change current good status`() {
        addGood(status = GoodStatus.PROCESSED)
        assertEquals(GoodStatus.PROCESSED, checkData?.getCurrentGood()?.getStatus())
    }

    @Test
    fun `Set deleted shelf status by index`() {
        addShelf()
        addShelf()
        checkData?.setShelfStatusDeletedByIndex(0)
        checkData?.setShelfStatusDeletedByIndex(1)
        assertEquals(ShelfStatus.DELETED, checkData?.getCurrentSegment()?.shelves?.get(0)?.getStatus())
        assertEquals(ShelfStatus.DELETED, checkData?.getCurrentSegment()?.shelves?.get(1)?.getStatus())
    }

    @Test
    fun `Set deleted shelf status by wrong index`() {
        addShelf() // Максимальный индекс - 0
        assertDoesNotThrow { checkData?.setShelfStatusDeletedByIndex(1) }
        assertDoesNotThrow { checkData?.setShelfStatusDeletedByIndex(2) }
    }

    @Test
    fun `Exist unsent data with empty data`() {
        assertEquals(false, checkData?.isExistUnsentData())
    }

    @Test
    fun `Exist unsent data with data`() {
        addSegment()
        assertEquals(true, checkData?.isExistUnsentData())
    }

    @Test
    fun `Not exist unfinished segments`() {
        addSegment(status = SegmentStatus.PROCESSED)
        assertEquals(false, checkData?.isExistUnfinishedSegment())
    }

    @Test
    fun `Exist unfinished segments`() {
        addSegment()
        assertEquals(true, checkData?.isExistUnfinishedSegment())
    }

    @Test
    fun `Set unfinished segment as current`() {
        addSegment() // Незавершенный сегмент
        addSegment(status = SegmentStatus.PROCESSED) // Текущий сегмент
        checkData?.setUnfinishedSegmentAsCurrent() // Делаем незавершенный сегмент текущим (индекс 1)
        assertEquals(1, checkData?.currentSegmentIndex)
    }

    @Test
    fun `Remove all finished segments`() {
        addSegment(number = customSegmentNumber)
        addSegment(status = SegmentStatus.PROCESSED)
        addSegment(status = SegmentStatus.DELETED)
        checkData?.removeAllFinishedSegments()
        assertEquals(1, checkData?.segments?.size)
        assertEquals(customSegmentNumber, checkData?.getCurrentSegment()?.number)
    }

    @Test
    fun `Current good at the top of the list`() {
        addGood()
        addGood(ean = customGoodEan) // Текущий товар
        assertTrue(checkData?.getFirstGood()?.ean == customGoodEan)
    }

    @Test
    fun `Current good is not at the top of the list`() {
        addGood(ean = customGoodEan)
        addGood() // Текущий товар
        checkData?.currentGoodIndex = 1 // Меняем текущий товар
        assertFalse(checkData?.getFirstGood()?.ean == customGoodEan)
    }

    @Test
    fun `Get previous good facings if same good`() {
        addGood(ean = customGoodEan, facings = 15) // Одинаковый предыдущий товар
        addGood(ean = customGoodEan, facings = 17) // Текущий товар
        assertEquals(15, checkData?.getPreviousSameGoodFacings())
    }

    @Test
    fun `Get previous good facings if not same good`() {
        addGood(facings = 15) // Предыдущий товар
        addGood(facings = 17) // Текущий товар
        assertEquals(0, checkData?.getPreviousSameGoodFacings())
    }

    @Test
    fun `Remove current good if same previous`() {
        addGood(ean = customGoodEan) // Одинаковый предыдущий товар
        addGood(ean = customGoodEan) // Текущий товар
        checkData?.removeCurrentGoodIfSamePrevious()
        assertEquals(1, checkData?.getCurrentShelf()?.goods?.size)
    }

    @Test
    fun `Remove current good if not same previous`() {
        addGood() // Предыдущий товар
        addGood() // Текущий товар
        checkData?.removeCurrentGoodIfSamePrevious()
        assertEquals(2, checkData?.getCurrentShelf()?.goods?.size)
    }

    @Test
    fun `Get market number without zeros`() {
        checkData?.marketNumber = "0012"
        assertEquals("12", checkData?.getFormattedMarketNumber())
    }

    @Test
    fun `Creation xml with check results`() {

    }




    @Test
    fun `Dummy`() {
    }
}