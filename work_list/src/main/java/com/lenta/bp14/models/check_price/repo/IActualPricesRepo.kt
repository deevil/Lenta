package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.*
import kotlin.random.Random

interface IActualPricesRepo {

    fun getActualPriceInfoFromCache(eanCode: String): IActualPriceInfo?
    fun getActualPriceInfoByMatNumber(matNumber: String): IActualPriceInfo?
    suspend fun getActualPriceInfoByEan(eanCode: String): Either<Failure, IActualPriceInfo>
    suspend fun getActualPriceInfoByMatNr(matNumber: String): Either<Failure, IActualPriceInfo>

}

class ActualPriceRepoForTest : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, IActualPriceInfo?>()

    //TODO удалить эту логику после создания фмников
    private lateinit var testResult: IScanPriceInfo

    fun putTestResult(priceInfo: IScanPriceInfo) {
        testResult = priceInfo
        Logg.d { "testResult: $testResult" }
    }

    override fun getActualPriceInfoFromCache(eanCode: String): IActualPriceInfo? {
        if (!cashedResults.containsKey(eanCode)) {
            cashedResults[eanCode] = null
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    getActualPriceInfoByEan(eanCode)
                }
            }
        }

        return cashedResults[eanCode]

    }

    override suspend fun getActualPriceInfoByEan(eanCode: String): Either<Failure, IActualPriceInfo> {
        //TODO реализовать запрос к серверу после реализации ФМников
        delay(500)
        return Either.Right(getPriceInfoForTest(testResult).apply {
            Logg.d { "actualPriceInfo: ${this}" }
            cashedResults[matNumber] = this
        })
    }

    override suspend fun getActualPriceInfoByMatNr(matNumber: String): Either<Failure, IActualPriceInfo> {
        //TODO реализовать запрос к серверу после реализации ФМников
        delay(500)
        return Either.Right(getPriceInfoForTest(testResult).apply {
            Logg.d { "actualPriceInfo: ${this}" }
            cashedResults[matNumber] = this
        })
    }


    override fun getActualPriceInfoByMatNumber(matNumber: String): IActualPriceInfo? {
        return cashedResults.values.firstOrNull { it?.matNumber == matNumber }
    }

    private fun getPriceInfoForTest(priceInfo: IScanPriceInfo): IActualPriceInfo {
        return if (Random.nextBoolean())
            ActualPriceInfo(
                    matNumber = priceInfo.eanCode,
                    productName = "Лук репчатый",
                    price1 = priceInfo.price ?: 23.56F,
                    price2 = priceInfo.discountCardPrice ?: 23.56F,
                    price3 = null,
                    price4 = null
            )
        else ActualPriceInfo(
                matNumber = priceInfo.eanCode,
                productName = "Мед липовый",
                price1 = (priceInfo.price ?: 0F) + 1,
                price2 = priceInfo.discountCardPrice ?: 23.56F,
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

}