package com.lenta.bp14.models.check_price

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.check_price.repo.ICheckPriceResultsRepo
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.platform.time.ITimeMonitor

class CheckPriceService(
        private val actualPricesRepo: IActualPricesRepo,
        private val readyResultsRepo: ICheckPriceResultsRepo,
        private val priceInfoParser: IPriceInfoParser,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckPriceService, StateFromToString {


    override fun checkProduct(rawCode: String?): ICheckPriceResult? {

        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode) ?: return null

        val actualPriceInfo = actualPricesRepo.getActualPriceInfo(scannedPriceInfo.eanCode
                ?: return null) ?: return null

        return CheckPriceResult(
                ean = scannedPriceInfo.eanCode!!,
                matNr = "",
                time = timeMonitor.getUnixTime(),
                scannedPriceInfo = scannedPriceInfo,
                actualPriceInfo = actualPriceInfo
        )

    }

    override fun getCheckResults(): LiveData<List<ICheckPriceResult>> {
        return readyResultsRepo.getCheckPriceResults()
    }

    override fun stateFromString(state: String) {
        //TODO будет реализовано позже
    }

    override fun stateToString(): String {
        //TODO будет реализовано позже
        return ""
    }

}


interface ICheckPriceService {
    fun checkProduct(rawCode: String?): ICheckPriceResult?
    fun getCheckResults(): LiveData<List<ICheckPriceResult>>

}

data class CheckPriceResult(
        override val ean: String,
        override val matNr: String,
        override val time: Long,
        override val scannedPriceInfo: IPriceInfo,
        override val actualPriceInfo: IPriceInfo
) : ICheckPriceResult

data class PriceInfo(
        override val eanCode: String?,
        override val matNr: String?,
        override val price: Float,
        override val discountCardPrice: Float
) : IPriceInfo

interface ICheckPriceResult {
    val ean: String
    val matNr: String
    val time: Long
    val scannedPriceInfo: IPriceInfo
    val actualPriceInfo: IPriceInfo

}

interface IPriceInfo {
    val eanCode: String?
    val matNr: String?
    val price: Float
    val discountCardPrice: Float
}