package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.IPriceInfo
import com.lenta.bp14.models.check_price.PriceInfo
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

interface IActualPricesRepo {

    fun getActualPriceInfo(eanCode: String): IPriceInfo?

}

class ActualPriceRepoForTest : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, IPriceInfo>()

    private var testResult: IPriceInfo? = null

    //TODO удалить эту логику после создания фмников
    fun putTestResult(priceInfo: IPriceInfo) {
        testResult = priceInfo
        Logg.d { "testResult: $testResult" }
    }

    override fun getActualPriceInfo(eanCode: String): IPriceInfo? {
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

    private fun getPriceInfoForTest(priceInfo: IPriceInfo?): IPriceInfo? {
        priceInfo?.let {
            return if (Random.nextBoolean())
                PriceInfo(
                        eanCode = it.eanCode,
                        matNr = it.eanCode,
                        nameOfProduct = null,
                        price = it.price,
                        discountCardPrice = it.discountCardPrice
                )
            else PriceInfo(
                    eanCode = it.eanCode,
                    matNr = it.eanCode,
                    nameOfProduct = null,
                    price = it.price + 1,
                    discountCardPrice = it.discountCardPrice
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