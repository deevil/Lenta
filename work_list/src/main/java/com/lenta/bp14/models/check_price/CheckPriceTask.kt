package com.lenta.bp14.models.check_price

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.check_price.repo.ICheckPriceResultsRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.bp14.requests.check_price.CheckPriceReport
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.utilities.extentions.map
import kotlin.math.min

class CheckPriceTask(
        private val taskDescription: CheckPriceTaskDescription,
        private val actualPricesRepo: IActualPricesRepo,
        private val readyResultsRepo: ICheckPriceResultsRepo,
        private val priceInfoParser: IPriceInfoParser,
        private val gson: Gson,
        override var processingMatNumber: String? = null,
        private val soundPlayer: ISoundPlayer,
        private val vibrateHelper: IVibrateHelper
) : ICheckPriceTask, StateFromToString {


    override fun checkProductFromVideoScan(rawCode: String?): ICheckPriceResult? {

        //Logg.d { "checkProductFromVideoScan: $rawCode" }

        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode) ?: return null

        val actualPriceInfo = actualPricesRepo.getActualPriceInfoFromCache(
                tkNumber = taskDescription.tkNumber,
                eanCode = scannedPriceInfo.eanCode
        )
                ?: return null

        return CheckPriceResult(
                ean = scannedPriceInfo.eanCode,
                matNr = actualPriceInfo.matNumber,
                name = actualPriceInfo.productName,
                scannedPriceInfo = scannedPriceInfo,
                actualPriceInfo = actualPriceInfo,
                userPriceInfo = null,
                isPrinted = false
        ).apply {
            readyResultsRepo.addCheckPriceResult(this).let { isAdded ->
                if (isAdded) {
                    vibrateHelper.shortVibrate()
                    if (this.isAllValid() == true) {
                        soundPlayer.playBeep()
                    } else {
                        soundPlayer.playError()
                    }
                }
            }
        }

    }

    override suspend fun checkPriceByQrCode(qrCode: String): Either<Failure, IActualPriceInfo> {
        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(qrCode)
                ?: return Either.Left(Failure.NotValidQrCode)

        return actualPricesRepo.getActualPriceInfoByEan(
                tkNumber = taskDescription.tkNumber,
                eanCode = scannedPriceInfo.eanCode
        ).also {
            it.either({}, { actualPriceInfo ->
                CheckPriceResult(
                        ean = scannedPriceInfo.eanCode,
                        matNr = actualPriceInfo.matNumber,
                        name = actualPriceInfo.productName,
                        scannedPriceInfo = scannedPriceInfo,
                        actualPriceInfo = actualPriceInfo,
                        userPriceInfo = null,
                        isPrinted = false
                ).apply {
                    readyResultsRepo.addCheckPriceResult(this)
                }
            })
        }


    }


    override fun removeCheckResultsByMatNumbers(matNumbers: Set<String>) {
        readyResultsRepo.removePriceCheckResults(matNumbers)
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

    override fun getProcessingActualPrice(): IActualPriceInfo? {
        return actualPricesRepo.getActualPriceInfoByMatNumber(matNumber = processingMatNumber
                ?: return null)
    }

    override fun setCheckPriceStatus(isValid: Boolean?) {
        readyResultsRepo.getCheckPriceResult(matNr = processingMatNumber).apply {

            if (this == null) {
                actualPricesRepo.getActualPriceInfoByMatNumber(processingMatNumber
                        ?: "")?.let { actualPriceInfo ->
                    readyResultsRepo.addCheckPriceResult(
                            CheckPriceResult(
                                    ean = actualPriceInfo.matNumber,
                                    matNr = actualPriceInfo.matNumber,
                                    name = actualPriceInfo.productName,
                                    scannedPriceInfo = ScanPriceInfo(
                                            eanCode = actualPriceInfo.matNumber,
                                            price = null,
                                            discountCardPrice = null
                                    ),
                                    actualPriceInfo = actualPriceInfo,
                                    userPriceInfo = UserPriceInfo(isValidPrice = isValid),
                                    isPrinted = false

                            )

                    )
                }

            } else {
                readyResultsRepo.addCheckPriceResult(
                        checkPriceResult = (this as CheckPriceResult).copy(
                                userPriceInfo = UserPriceInfo(isValidPrice = isValid)
                        )
                )
            }


        }
    }

    override fun getCheckResultsForPrint(): LiveData<List<ICheckPriceResult>> {
        return getCheckResults().map { checkResults ->
            checkResults?.filter {
                !it.isPrinted && !(it.isAllValid() ?: false)
            }
        }

    }

    override suspend fun getActualPriceByEan(eanCode: String): Either<Failure, IActualPriceInfo> {
        return actualPricesRepo.getActualPriceInfoByEan(
                tkNumber = taskDescription.tkNumber,
                eanCode = eanCode
        )
    }

    override suspend fun getActualPriceByMatNr(matNumber: String): Either<Failure, IActualPriceInfo> {
        return actualPricesRepo.getActualPriceInfoByMatNr(
                tkNumber = taskDescription.tkNumber,
                matNumber = matNumber
        )
    }

    override fun getReportData(ip: String): CheckPriceReport {
        return CheckPriceReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = false,
                checksResults = getCheckResults().value ?: emptyList()

        )
    }

}

fun ICheckPriceResult?.toCheckStatus(): CheckStatus? {
    return when (this?.isAllValid()) {
        true -> CheckStatus.VALID
        false -> CheckStatus.NOT_VALID
        null -> CheckStatus.ERROR
    }
}

interface ICheckPriceTask : ITask {
    fun checkProductFromVideoScan(rawCode: String?): ICheckPriceResult?
    fun getCheckResults(): LiveData<List<ICheckPriceResult>>
    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)
    fun getProcessingActualPrice(): IActualPriceInfo?
    fun setCheckPriceStatus(isValid: Boolean?)
    fun getCheckResultsForPrint(): LiveData<List<ICheckPriceResult>>
    suspend fun getActualPriceByEan(eanCode: String): Either<Failure, IActualPriceInfo>
    suspend fun getActualPriceByMatNr(matNumber: String): Either<Failure, IActualPriceInfo>
    suspend fun checkPriceByQrCode(qrCode: String): Either<Failure, IActualPriceInfo>
    fun getReportData(ip: String): CheckPriceReport

    var processingMatNumber: String?

}

data class CheckPriceResult(
        override val ean: String,
        override val matNr: String?,
        override val name: String?,
        override val scannedPriceInfo: IScanPriceInfo,
        override val actualPriceInfo: IActualPriceInfo,
        override val userPriceInfo: IUserPriceInfo?,
        override val isPrinted: Boolean
) : ICheckPriceResult {

    override fun isPriceValid(): Boolean? {
        if (userPriceInfo != null) {
            return userPriceInfo.isValidPrice
        }
        return scannedPriceInfo.price == actualPriceInfo.getPrice()
    }

    override fun isDiscountPriceValid(): Boolean? {
        return scannedPriceInfo.discountCardPrice == actualPriceInfo.getDiscountCardPrice()
    }

    override fun isAllValid(): Boolean? {
        if (userPriceInfo != null) {
            return userPriceInfo.isValidPrice
        }
        val isPriceValid = isPriceValid()
        val isDiscountPriceValid = isDiscountPriceValid()

        if (isPriceValid == null || isDiscountPriceValid == null) {
            return null
        }
        return isPriceValid && isDiscountPriceValid
    }


}

data class ScanPriceInfo(
        override val eanCode: String,
        override val price: Float?,
        override val discountCardPrice: Float?
) : IScanPriceInfo

data class ActualPriceInfo(
        override val matNumber: String,
        override val productName: String?,
        override val price1: Float?,
        override val price2: Float?,
        override val price3: Float?,
        override val price4: Float?
) : IActualPriceInfo {
    override fun getPrice(): Float? {
        return price1
    }

    override fun getDiscountCardPrice(): Float? {
        return when {
            price2 != null && price3 != null && price4 != null -> min(min(price2, price3), price4)
            price2 != null && price3 == null && price4 == null -> price2
            price2 != null && price3 != null -> price3
            price2 != null && price4 != null -> price4
            else -> null
        }
    }
}

interface ICheckPriceResult {
    val ean: String
    val matNr: String?
    val name: String?
    val scannedPriceInfo: IScanPriceInfo
    val actualPriceInfo: IActualPriceInfo
    val userPriceInfo: IUserPriceInfo?
    fun isPriceValid(): Boolean?
    fun isDiscountPriceValid(): Boolean?
    fun isAllValid(): Boolean?
    val isPrinted: Boolean
}


interface IScanPriceInfo {
    val eanCode: String
    val price: Float?
    val discountCardPrice: Float?
}

interface IActualPriceInfo {
    val matNumber: String
    val productName: String?
    val price1: Float?
    val price2: Float?
    val price3: Float?
    val price4: Float?
    fun getPrice(): Float?
    fun getDiscountCardPrice(): Float?
}

data class UserPriceInfo(override val isValidPrice: Boolean?) : IUserPriceInfo


interface IUserPriceInfo {
    /**
     * возвращает null, если нет ценника
     */
    val isValidPrice: Boolean?
}