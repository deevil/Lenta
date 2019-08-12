package com.lenta.bp7.data.model

import com.lenta.bp7.data.CheckType
import com.lenta.bp7.data.IPersistCheckResult
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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

    private fun addSegment(number: String = "" + (100..999).random() + "-" + (100..999).random()) {
        checkData?.addSegment(marketNumber, number)
    }

    private fun addShelf(number: String = "" + (1..999).random()) {
        if (checkData?.getCurrentSegment() == null) addSegment()
        checkData?.addShelf(number)
    }

    private fun addGood(goodInfo: GoodInfo = getCustomGoodInfo()) {
        if (checkData?.getCurrentSegment() == null) addSegment()
        if (checkData?.getCurrentShelf() == null) addShelf()
        checkData?.addGood(goodInfo)
    }

    private fun getCustomGoodInfo(
            ean: String = "" + (10000000..99999999999999).random(),
            material: String = "000000000000" + (100000..999999).random(),
            matcode: String = "" + (100000000000..999999999999).random(),
            enteredCode: EnteredCode = EnteredCode.EAN,
            name: String = "Good " + (1..999).random(),
            unitsCode: String = "ST",
            units: String = "шт"
    ): GoodInfo {
        return GoodInfo(ean, material, matcode, enteredCode, name, unitsCode, units)
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
        addGood(getCustomGoodInfo(ean = customGoodEan)) // Текущий товар
        assertEquals(customGoodEan, checkData?.getCurrentGood()?.ean)
    }

    @Test
    fun `Get changed current good`() {
        addGood()
        addGood(getCustomGoodInfo(ean = customGoodEan))
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
        addGood(getCustomGoodInfo(ean = customGoodEan)) // Предыдущий товар
        addGood() // Текущий товар
        assertEquals(customGoodEan, checkData?.getPreviousGood()?.ean)
    }

    @Test
    fun `Get previous good with changed current element`() {
        addGood(getCustomGoodInfo(ean = customGoodEan))
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
        addGood(getCustomGoodInfo(ean = customGoodEan)) // Первый товар в списке
        assertEquals(customGoodEan, checkData?.getFirstGood()?.ean)
    }

    @Test
    fun `Get second good from empty list`() {
        assertEquals(null, checkData?.getSecondGood()?.ean)
    }

    @Test
    fun `Get second good from list`() {
        addGood(getCustomGoodInfo(ean = customGoodEan)) // Второй товар в списке
        addGood() // Первый товар в списке
        assertEquals(customGoodEan, checkData?.getSecondGood()?.ean)
    }





    @Test
    fun `Dummy`() {
    }
}