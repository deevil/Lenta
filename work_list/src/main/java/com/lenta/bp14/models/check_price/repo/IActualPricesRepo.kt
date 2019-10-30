package com.lenta.bp14.models.check_price.repo

import com.lenta.bp14.models.check_price.*
import com.lenta.bp14.requests.check_price.CheckPriceRequestParams
import com.lenta.bp14.requests.check_price.ICheckPriceNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import kotlinx.coroutines.*
import javax.inject.Inject

interface IActualPricesRepo {

    fun getActualPriceInfoFromCache(tkNumber: String, eanCode: String): ActualPriceInfo?
    fun getActualPriceInfoByMatNumber(matNumber: String): ActualPriceInfo?
    suspend fun getActualPriceInfoByEan(tkNumber: String, eanCode: String): Either<Failure, ActualPriceInfo>
    suspend fun getActualPriceInfoByMatNr(tkNumber: String, matNumber: String): Either<Failure, ActualPriceInfo>
    fun addToCacheActualPriceInfo(actualPriceinfo: ActualPriceInfo)

}

class ActualPriceRepo @Inject constructor(private val checkPriceRequest: ICheckPriceNetRequest) : IActualPricesRepo {

    private val cashedResults = mutableMapOf<String, ActualPriceInfo?>()

    override fun addToCacheActualPriceInfo(actualPriceinfo: ActualPriceInfo) {
        cashedResults[actualPriceinfo.matNumber] = actualPriceinfo
    }


    override fun getActualPriceInfoFromCache(tkNumber: String, eanCode: String): ActualPriceInfo? {
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

    override suspend fun getActualPriceInfoByEan(tkNumber: String, eanCode: String): Either<Failure, ActualPriceInfo> {
        return checkPriceRequest(CheckPriceRequestParams(
                tkNumber = tkNumber,
                ean = eanCode,
                matNr = null
        )).map {
            cashedResults[eanCode] = it
            it
        }
    }

    override suspend fun getActualPriceInfoByMatNr(tkNumber: String, matNumber: String): Either<Failure, ActualPriceInfo> {
        return checkPriceRequest(CheckPriceRequestParams(
                tkNumber = tkNumber,
                ean = null,
                matNr = matNumber
        )).map {
            cashedResults[matNumber] = it
            it
        }
    }


    override fun getActualPriceInfoByMatNumber(matNumber: String): ActualPriceInfo? {
        return cashedResults.values.firstOrNull { it?.matNumber == matNumber }
    }


}