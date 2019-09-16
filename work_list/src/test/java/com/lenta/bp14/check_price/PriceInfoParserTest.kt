package com.lenta.bp14.check_price

import com.lenta.bp14.models.check_price.PriceInfoParser
import junit.framework.TestCase.assertNull
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Test

@Suppress("NonAsciiCharacters")
class PriceInfoParserTest {
    private lateinit var priceInfoParser: PriceInfoParser
    private var rawCode: String? = null


    @org.junit.Before
    fun setup() {
        priceInfoParser = PriceInfoParser()
    }

    //TODO после окончательного утверждения формата проверить, что логика парсинга отражена в ТП
    @Test
    fun `Парсинг валидного кода`() {

        rawCode = "(01)4015000993678(390y)203,69(392y)193,49"

        val expectedEan = "4015000993678"
        val expectedPrice = 203.69F
        val expectedDiscountCardPrice = 193.49F

        val priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNotNull(priceInfo)
        priceInfo!!
        assertThat(priceInfo.eanCode, `is`(equalTo(expectedEan)))
        assertThat(priceInfo.price, `is`(equalTo(expectedPrice)))
        assertThat(priceInfo.discountCardPrice, `is`(equalTo(expectedDiscountCardPrice)))

    }

    @Test
    fun `Парсинг кода без EAN`() {

        rawCode = "(01)4015000993678(390y)193,49(392y)"

        var priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

        rawCode = "(392y)193,49"

        priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

    }

    @Test
    fun `Парсинг кода без обычной цены`() {

        rawCode = "(01)4015000993678(390y)193,49(392y)"

        var priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

        rawCode = "(01)4015000993678(392y)193,49"

        priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

    }

    @Test
    fun `Парсинг кода без акционной цены`() {

        rawCode = "(01)4015000993678(390y)193,49(392y)"

        var priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

        rawCode = "(01)4015000993678(390y)193,49"

        priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

    }

    @Test
    fun `Парсинг каких-то букв`() {

        rawCode = "авыюоалдоывал(01)4015000993678"

        val priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

    }

    @Test
    fun `Парсинг каких-то цифр`() {

        rawCode = "120,5"

        val priceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode)

        assertNull(priceInfo)

    }


}