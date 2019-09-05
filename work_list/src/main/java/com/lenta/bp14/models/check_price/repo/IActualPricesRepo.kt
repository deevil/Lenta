package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.IPriceInfo
import com.lenta.bp14.models.check_price.PriceInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

interface IActualPricesRepo {

    fun getActualPriceInfo(eanCode: String): IPriceInfo?

}

class ActualPriceRepoForTest : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, IPriceInfo>()

    private val scannedResults = mutableMapOf<String, IPriceInfo>()

    fun putTestResult(priceInfo: IPriceInfo) {
        scannedResults.put(priceInfo.eanCode ?: "", priceInfo)
    }

    override fun getActualPriceInfo(eanCode: String): IPriceInfo? {
        return cashedResults[eanCode].apply {
            if (this == null) {
                GlobalScope.launch {
                    delay(500)
                    getPriceInfoForTest(eanCode)?.let {
                        cashedResults[eanCode] = it
                    }
                }
            }
        }
    }

    private fun getPriceInfoForTest(eanCode: String): IPriceInfo? {
        scannedResults[eanCode]?.let {
            return if (Random.nextBoolean()) it else PriceInfo(
                    eanCode = it.eanCode,
                    matNr = "000000000000002216",
                    price = it.price + 1,
                    discountCardPrice = it.discountCardPrice
            )
        }
        return null
    }

}