package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.*
import com.lenta.bp14.requests.check_price.CheckPriceRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import kotlinx.coroutines.*

interface IActualPricesRepo {

    fun getActualPriceInfoFromCache(tkNumber: String, eanCode: String): IActualPriceInfo?
    fun getActualPriceInfoByMatNumber(matNumber: String): IActualPriceInfo?
    suspend fun getActualPriceInfoByEan(tkNumber: String, eanCode: String): Either<Failure, IActualPriceInfo>
    suspend fun getActualPriceInfoByMatNr(tkNumber: String, matNumber: String): Either<Failure, IActualPriceInfo>

}

class ActualPriceRepoForTest(private val checkPriceRequest: UseCase<ActualPriceInfo, CheckPriceRequestParams>) : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, IActualPriceInfo?>()


    override fun getActualPriceInfoFromCache(tkNumber: String, eanCode: String): IActualPriceInfo? {
        if (!cashedResults.containsKey(eanCode)) {
            cashedResults[eanCode] = null
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    getActualPriceInfoByEan(
                            tkNumber = tkNumber, eanCode = eanCode
                    )
                }
            }
        }

        return cashedResults[eanCode]

    }

    override suspend fun getActualPriceInfoByEan(tkNumber: String, eanCode: String): Either<Failure, IActualPriceInfo> {
        return checkPriceRequest(CheckPriceRequestParams(
                tkNumber = tkNumber,
                ean = eanCode,
                matNr = null
        )).map {
            cashedResults[eanCode] = it
            it
        }
    }

    override suspend fun getActualPriceInfoByMatNr(tkNumber: String, matNumber: String): Either<Failure, IActualPriceInfo> {
        return checkPriceRequest(CheckPriceRequestParams(
                tkNumber = tkNumber,
                ean = null,
                matNr = matNumber
        )).map {
            cashedResults[matNumber] = it
            it
        }
    }


    override fun getActualPriceInfoByMatNumber(matNumber: String): IActualPriceInfo? {
        return cashedResults.values.firstOrNull { it?.matNumber == matNumber }
    }


}