package com.lenta.bp14.check_price

import com.lenta.bp14.models.check_price.ActualPriceInfo
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Test

@Suppress("NonAsciiCharacters")
class DiscountPriceTest {

    lateinit var actualPriceInfo: ActualPriceInfo

    val PRICE_1 = 40.0
    val PRICE_2 = 30.0
    val PRICE_3 = 20.0
    val PRICE_4 = 10.0
    val MIN_PRICE = 10.0

    @Test
    fun `Нет ни одной цены`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = null,
                price3 = null,
                price4 = null
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsNull())

    }


    @Test
    fun `Есть только price2`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_2,
                price3 = null,
                price4 = null
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual.equalTo(PRICE_2))

    }

    @Test
    fun `Есть только price3`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = null,
                price3 = PRICE_3,
                price4 = null
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsNull())

    }

    @Test
    fun `Есть только price4`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = null,
                price3 = null,
                price4 = PRICE_4
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsNull())

    }

    @Test
    fun `Есть price2 и price3`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_2,
                price3 = PRICE_3,
                price4 = null
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(PRICE_3))

    }

    @Test
    fun `Есть price2 и price4`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_2,
                price3 = null,
                price4 = PRICE_4
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(PRICE_4))

    }

    @Test
    fun `Есть price3 и price4`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = null,
                price3 = PRICE_3,
                price4 = PRICE_4
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsNull())

    }

    @Test
    fun `Есть все`() {

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_2,
                price3 = PRICE_3,
                price4 = PRICE_4
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(MIN_PRICE))

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_3,
                price3 = PRICE_2,
                price4 = PRICE_4
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(MIN_PRICE))

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_2,
                price3 = PRICE_4,
                price4 = PRICE_3
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(MIN_PRICE))

        actualPriceInfo = ActualPriceInfo(
                matNumber = "",
                productName = null,
                price1 = PRICE_1,
                price2 = PRICE_4,
                price3 = PRICE_3,
                price4 = PRICE_2
        )

        Assert.assertThat(actualPriceInfo.getDiscountCardPrice(), IsEqual(MIN_PRICE))

    }


}