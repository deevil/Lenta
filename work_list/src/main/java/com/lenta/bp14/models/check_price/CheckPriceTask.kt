package com.lenta.bp14.models.check_price

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.di.CheckPriceScope
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.check_price.repo.ICheckPriceResultsRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.bp14.requests.check_price.CheckPriceReport
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject
import kotlin.math.min

@CheckPriceScope
class CheckPriceTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val taskDescription: CheckPriceTaskDescription,
        private val actualPricesRepo: IActualPricesRepo,
        private val readyResultsRepo: ICheckPriceResultsRepo,
        private val priceInfoParser: IPriceInfoParser,
        private val gson: Gson,
        private val soundPlayer: ISoundPlayer,
        private val vibrateHelper: IVibrateHelper
) : ICheckPriceTask, StateFromToString {

    private val processingProducts by lazy {
        getCheckResults().map { processedGoodInfo ->
            val processedMaterials = processedGoodInfo?.map { it.matNr }?.toSet() ?: emptySet()
            val positions = taskDescription.additionalTaskInfo?.positions?.filter { !processedMaterials.contains(it.matNr) }
                    ?: emptyList()
            positions.map {
                getCheckPriceByMatnr(it.matNr)
            }
        }
    }

    private val productsInfoMap by lazy {
        taskDescription.additionalTaskInfo?.productsInfo?.map { it.matNr to it }?.toMap()
                ?: emptyMap()
    }

    private val priceInfoMap by lazy {
        taskDescription.additionalTaskInfo?.prices?.map { it.matnr to it }?.toMap()
                ?: emptyMap()
    }

    private val checkPriceMap by lazy {
        taskDescription.additionalTaskInfo?.checkPrices?.map { it.matNr to it }?.toMap()
                ?: emptyMap()
    }

    init {
        initProcessed()
    }

    private fun initProcessed() {

        taskDescription.additionalTaskInfo?.prices?.forEach {
            val productInfo = productsInfoMap[it.matnr]
            actualPricesRepo.addToCacheActualPriceInfo(
                    ActualPriceInfo(
                            matNumber = it.matnr,
                            productName = productInfo?.name,
                            price1 = it.price1,
                            price2 = it.price2,
                            price3 = it.price3,
                            price4 = it.price4
                    )
            )
        }

        taskDescription.additionalTaskInfo?.checkPrices?.let {
            it.forEach { checkPrice ->
                val productInfo = productsInfoMap[checkPrice.matNr]
                readyResultsRepo.addCheckPriceResult(
                        getCheckPriceByMatnr(checkPrice.matNr)
                )
            }
        }
    }

    override var processingMatNumber: String? = null

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

    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.CheckPrice.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProcessingActualPrice(): IActualPriceInfo? {
        return actualPricesRepo.getActualPriceInfoByMatNumber(matNumber = processingMatNumber
                ?: return null)
    }

    override fun getToProcessingProducts(): LiveData<List<ICheckPriceResult>> {
        return processingProducts
    }

    private fun getCheckPriceByMatnr(matNr: String): ICheckPriceResult {
        val productInfo = productsInfoMap[matNr]
        val priceInfo = priceInfoMap[matNr]
        val checkPriceInfo = checkPriceMap[matNr]
        val emptyPriceInfo = ScanPriceInfo(
                eanCode = matNr,
                price = null,
                discountCardPrice = null
        )
        return CheckPriceResult(
                ean = "",
                matNr = matNr,
                name = productInfo?.name,
                scannedPriceInfo = emptyPriceInfo,
                actualPriceInfo = ActualPriceInfo(
                        matNumber = matNr,
                        productName = productInfo?.name,
                        price1 = priceInfo?.price1,
                        price2 = priceInfo?.price2,
                        price3 = priceInfo?.price3,
                        price4 = priceInfo?.price4
                ),
                userPriceInfo = UserPriceInfo(
                        isValidPrice = when (checkPriceInfo?.statCheck) {
                            "1" -> true
                            "2" -> false
                            else -> null
                        }
                ),
                isPrinted = checkPriceInfo?.isPrinted.isSapTrue()
        )
    }

    override fun setCheckPriceStatus(isValid: Boolean?) {
        setCheckPriceStatus(isValid, processingMatNumber ?: "")

    }

    private fun setCheckPriceStatus(isValid: Boolean?, matNr: String) {
        readyResultsRepo.getCheckPriceResult(matNr = matNr).apply {

            if (this == null) {
                actualPricesRepo.getActualPriceInfoByMatNumber(matNr)?.let { actualPriceInfo ->
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

    override fun getReportData(ip: String, isNotFinish: Boolean): CheckPriceReport {
        return CheckPriceReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = isNotFinish,
                checksResults = getCheckResults().value ?: emptyList()

        )
    }


    override fun isEmpty(): Boolean {
        return readyResultsRepo.getCheckPriceResults().value.isNullOrEmpty()
    }

    override fun isHaveDiscrepancies(): Boolean {
        return processingProducts.value?.isNotEmpty() == true
    }

    override fun getListOfDifferences(): LiveData<List<BaseProductInfo>> {
        return processingProducts.map { list ->
            list?.map { item ->
                BaseProductInfo(
                        matNr = item.matNr ?: "",
                        name = item.name ?: ""
                )
            }
        }
    }

    override fun setMissing(matNrList: List<String>) {
        matNrList.forEach {
            setCheckPriceStatus(null, matNr = it)
        }
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
    fun getToProcessingProducts(): LiveData<List<ICheckPriceResult>>
    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)
    fun getProcessingActualPrice(): IActualPriceInfo?
    fun setCheckPriceStatus(isValid: Boolean?)
    fun getCheckResultsForPrint(): LiveData<List<ICheckPriceResult>>
    suspend fun getActualPriceByEan(eanCode: String): Either<Failure, IActualPriceInfo>
    suspend fun getActualPriceByMatNr(matNumber: String): Either<Failure, IActualPriceInfo>
    suspend fun checkPriceByQrCode(qrCode: String): Either<Failure, IActualPriceInfo>
    fun getReportData(ip: String, isNotFinish: Boolean): CheckPriceReport

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
        override val price: Double?,
        override val discountCardPrice: Double?
) : IScanPriceInfo

data class ActualPriceInfo(
        override val matNumber: String,
        override val productName: String?,
        override val price1: Double?,
        override val price2: Double?,
        override val price3: Double?,
        override val price4: Double?
) : IActualPriceInfo {
    override fun getPrice(): Double? {
        return price1
    }

    override fun getDiscountCardPrice(): Double? {
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
    val price: Double?
    val discountCardPrice: Double?
}

interface IActualPriceInfo {
    val matNumber: String
    val productName: String?
    val price1: Double?
    val price2: Double?
    val price3: Double?
    val price4: Double?
    fun getPrice(): Double?
    fun getDiscountCardPrice(): Double?
}

data class UserPriceInfo(override val isValidPrice: Boolean?) : IUserPriceInfo


interface IUserPriceInfo {
    /**
     * возвращает null, если нет ценника
     */
    val isValidPrice: Boolean?
}