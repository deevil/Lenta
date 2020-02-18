package com.lenta.bp16.features.good_weighing

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import com.lenta.bp16.features.good_weighing.GoodWeighingViewModel as GoodWeighingViewModel1

class GoodWeighingViewModelTest {

    lateinit var goodWeighingViewModel: GoodWeighingViewModel1

    @Before
    fun setUp() {
        goodWeighingViewModel = GoodWeighingViewModel1()
    }

    @Test
    fun `Getting formatted weight`() {
        assertEquals(goodWeighingViewModel.getFormattedWeight(""), "000000")
        assertEquals(goodWeighingViewModel.getFormattedWeight("14"), "014000")
        assertEquals(goodWeighingViewModel.getFormattedWeight("1.2"), "001200")
        assertEquals(goodWeighingViewModel.getFormattedWeight("3.32"), "003320")
        assertEquals(goodWeighingViewModel.getFormattedWeight("15.241"), "015241")
        assertEquals(goodWeighingViewModel.getFormattedWeight("123.456"), "123456")
    }

    @Test
    fun `Getting a weight barcode`() {
        assertEquals(goodWeighingViewModel.getFormattedEan("2425308000000", 1.268), "2425308012681")
        assertEquals(goodWeighingViewModel.getFormattedEan("1234567000000", 12.3), "1234567123004")
    }

}