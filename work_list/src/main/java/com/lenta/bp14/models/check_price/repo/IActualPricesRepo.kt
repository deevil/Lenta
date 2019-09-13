package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.*
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

interface IActualPricesRepo {

    fun getActualPriceInfo(eanCode: String): IActualPriceInfo?

}

class ActualPriceRepoForTest : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, IActualPriceInfo>()

    private var testResult: IScanPriceInfo? = null

    //TODO удалить эту логику после создания фмников
    fun putTestResult(priceInfo: IScanPriceInfo) {
        testResult = priceInfo
        Logg.d { "testResult: $testResult" }
    }

    override fun getActualPriceInfo(eanCode: String): IActualPriceInfo? {
        return cashedResults[eanCode].apply {
            Logg.d { "getActualPriceInfo: $testResult" }
            if (this == null) {
                GlobalScope.launch {
                    delay(500)
                    if (!cashedResults.containsKey(eanCode)) {
                        getPriceInfoForTest(testResult)?.let {
                            cashedResults[eanCode] = it
                        }
                    }

                }
            }
        }
    }

    private fun getPriceInfoForTest(priceInfo: IScanPriceInfo?): IActualPriceInfo? {
        priceInfo?.let {
            return if (Random.nextBoolean())
                ActualPriceInfo(
                        matNumber = it.eanCode,
                        productName = "Лук репчатый",
                        price1 = it.price,
                        price2 = it.discountCardPrice,
                        price3 = null,
                        price4 = null
                )
            else ActualPriceInfo(
                    matNumber = it.eanCode,
                    productName = "Мед липовый",
                    price1 = (it.price ?: 0F) + 1,
                    price2 = it.discountCardPrice,
                    price3 = null,
                    price4 = null
            )
            /*return PriceInfo(
                    eanCode = it.eanCode,
                    matNr = "000000000000002216",
                    price = it.price,
                    discountCardPrice = it.discountCardPrice
            )*/
        }
        return null
    }

}