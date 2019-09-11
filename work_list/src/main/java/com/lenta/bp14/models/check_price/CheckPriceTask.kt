package com.lenta.bp14.models.check_price

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_price.repo.ActualPriceRepoForTest
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.check_price.repo.ICheckPriceResultsRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.implementationOf

class CheckPriceTask(
        private val taskDescription: CheckPriceTaskDescription,
        private val actualPricesRepo: IActualPricesRepo,
        private val readyResultsRepo: ICheckPriceResultsRepo,
        private val priceInfoParser: IPriceInfoParser,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckPriceTask, StateFromToString {


    override fun checkProduct(rawCode: String?): ICheckPriceResult? {

        Logg.d { "checkProduct: $rawCode" }

        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode) ?: return null

        actualPricesRepo.implementationOf(ActualPriceRepoForTest::class.java).apply {
            Logg.d { "ActualPriceRepoForTest: $this" }
        }?.putTestResult(scannedPriceInfo)

        val actualPriceInfo = actualPricesRepo.getActualPriceInfo(scannedPriceInfo.eanCode
                ?: return null) ?: return null

        return CheckPriceResult(
                ean = scannedPriceInfo.eanCode!!,
                matNr = actualPriceInfo.matNr,
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

    override fun getTaskType(): ITaskType {
        return TaskTypes.CheckPrice.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}

fun ICheckPriceResult?.toCheckStatus(): CheckStatus? {
    if (this == null) return null
    if (isErrorCheck()) {
        return CheckStatus.ERROR
    }
    return if (isAllValid()) CheckStatus.VALID else CheckStatus.NOT_VALID


}


interface ICheckPriceTask : ITask {
    fun checkProduct(rawCode: String?): ICheckPriceResult?
    fun getCheckResults(): LiveData<List<ICheckPriceResult>>

}

data class CheckPriceResult(
        override val ean: String,
        override val matNr: String?,
        override val time: Long,
        override val scannedPriceInfo: IPriceInfo,
        override val actualPriceInfo: IPriceInfo
) : ICheckPriceResult {
    override fun isPriceValid(): Boolean {
        return scannedPriceInfo.price == actualPriceInfo.price
    }

    override fun isDiscountPriceValid(): Boolean {
        return scannedPriceInfo.discountCardPrice == actualPriceInfo.discountCardPrice
    }

    override fun isAllValid(): Boolean {
        return isPriceValid() && isDiscountPriceValid()
    }

    override fun isErrorCheck(): Boolean {
        return actualPriceInfo.matNr.isNullOrBlank()
    }

}

data class PriceInfo(
        override val eanCode: String?,
        override val matNr: String?,
        override val price: Float,
        override val discountCardPrice: Float
) : IPriceInfo

interface ICheckPriceResult {
    val ean: String
    val matNr: String?
    val time: Long
    val scannedPriceInfo: IPriceInfo
    val actualPriceInfo: IPriceInfo
    fun isPriceValid(): Boolean
    fun isDiscountPriceValid(): Boolean
    fun isAllValid(): Boolean
    fun isErrorCheck(): Boolean

}

interface IPriceInfo {
    val eanCode: String?
    val matNr: String?
    val price: Float
    val discountCardPrice: Float
}