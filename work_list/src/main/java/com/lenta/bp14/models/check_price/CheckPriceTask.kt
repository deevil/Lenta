package com.lenta.bp14.models.check_price

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.di.CheckPriceScope
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.BaseProductInfo
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.check_price.repo.CheckPriceResultsRepo
import com.lenta.bp14.models.check_price.repo.IActualPricesRepo
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.print.PriceTagType
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.requests.check_price.CheckPriceReport
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.sound.ISoundPlayer
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toNullIfEmpty
import javax.inject.Inject
import kotlin.math.min

@CheckPriceScope
class CheckPriceTask @Inject constructor(
        private val generalRepo: IGeneralRepo,
        private val taskDescription: CheckPriceTaskDescription,
        private val actualPricesRepo: IActualPricesRepo,
        private val readyResultsRepo: CheckPriceResultsRepo,
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
                            price2 = it.price2.toNullIfEmpty(),
                            price3 = it.price3.toNullIfEmpty(),
                            price4 = it.price4.toNullIfEmpty(),
                            options = GoodOptions(
                                    matrixType = getMatrixType(productInfo?.matrixType.orEmpty()),
                                    section = if (productInfo?.sectionNumber?.isNotEmpty() == true) productInfo.sectionNumber else "91",
                                    goodType = getGoodType(
                                            alcohol = productInfo?.isAlco.orEmpty(),
                                            excise = productInfo?.isExcise.orEmpty(),
                                            marked = productInfo?.isMarked.orEmpty(),
                                            vrus = productInfo?.isVRus.orEmpty()),
                                    healthFood = productInfo?.isHealthyFood.isSapTrue(),
                                    novelty = productInfo?.isNew.isSapTrue()
                            )
                    )
            )
        }

        taskDescription.additionalTaskInfo?.checkPrices?.let {
            it.forEach { checkPrice ->
                readyResultsRepo.addCheckPriceResult(
                        getCheckPriceByMatnr(checkPrice.matNr)
                )
            }
        }
    }

    override var processingMatNumber: String? = null

    override fun checkProductFromVideoScan(rawCode: String?): CheckPriceResult? {
        //Logg.d { "checkProductFromVideoScan: $rawCode" }

        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(rawCode) ?: return null

        val actualPriceInfo = actualPricesRepo.getActualPriceInfoFromCache(
                tkNumber = taskDescription.tkNumber,
                eanCode = scannedPriceInfo.eanCode
        ) ?: return null

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

    override suspend fun checkPriceByQrCode(qrCode: String): Either<Failure, CheckPriceResult> {
        val scannedPriceInfo = priceInfoParser.getPriceInfoFromRawCode(qrCode)
                ?: return Either.Left(Failure.NotValidQrCode)

        return actualPricesRepo.getActualPriceInfoByEan(
                tkNumber = taskDescription.tkNumber,
                eanCode = scannedPriceInfo.eanCode
        ).rightToLeft(
                fnRtoL = checksForAddFunc
        ).map { actualPriceInfo ->
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
        }
    }

    override fun removeCheckResultsByMatNumbers(matNumbers: Set<String>) {
        readyResultsRepo.removePriceCheckResults(matNumbers)
    }


    override fun getCheckResults(): LiveData<List<CheckPriceResult>> {
        return readyResultsRepo.getCheckPriceResults()
    }

    override fun getTaskType(): ITaskTypeInfo {
        return generalRepo.getTasksTypeInfo(AppTaskTypes.CheckPrice.taskType)!!
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

    override fun getProcessingActualPrice(): ActualPriceInfo? {
        return actualPricesRepo.getActualPriceInfoByMatNumber(matNumber = processingMatNumber
                ?: return null)
    }

    override fun getToProcessingProducts(): LiveData<List<CheckPriceResult>> {
        return processingProducts
    }

    private fun getCheckPriceByMatnr(matNr: String): CheckPriceResult {
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
                        price2 = priceInfo?.price2.toNullIfEmpty(),
                        price3 = priceInfo?.price3.toNullIfEmpty(),
                        price4 = priceInfo?.price4.toNullIfEmpty(),
                        options = GoodOptions(
                                matrixType = getMatrixType(productInfo?.matrixType.orEmpty()),
                                section = if (productInfo?.sectionNumber?.isNotEmpty() == true) productInfo.sectionNumber else "91",
                                goodType = getGoodType(
                                        alcohol = productInfo?.isAlco.orEmpty(),
                                        excise = productInfo?.isExcise.orEmpty(),
                                        marked = productInfo?.isMarked.orEmpty(),
                                        vrus = productInfo?.isVRus.orEmpty()),
                                healthFood = productInfo?.isHealthyFood.isSapTrue(),
                                novelty = productInfo?.isNew.isSapTrue()
                        )
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
        setCheckPriceStatus(isValid, processingMatNumber.orEmpty())
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
                                    isPrinted = false,
                                    isMissing = isValid == null
                            )
                    )
                }
            } else {
                readyResultsRepo.addCheckPriceResult(
                        checkPriceResult = (this).copy(
                                userPriceInfo = UserPriceInfo(isValidPrice = isValid),
                                isMissing = isValid == null
                        )
                )
            }
        }
    }

    override fun getCheckResultsForPrint(priceTagType: LiveData<PriceTagType>): LiveData<List<CheckPriceResult>> {
        return getCheckResults().map { checkResults ->
            checkResults?.filter {
                !it.isPrinted && !(it.isAllValid() ?: false)
            }
        }.combineLatest(priceTagType).map {
            val forPrintTask = it!!.first
            val priceTagTypeValue = it.second
            return@map forPrintTask.filter { checkPriceResult ->
                checkPriceResult.isForPriceTag(priceTagTypeValue)
            }
        }
    }

    private val checksForAddFunc = { iActualPriceInfo: ActualPriceInfo ->
        if (!isAllowedProductForTask(iActualPriceInfo.matNumber)) {
            Failure.InvalidProductForTask
        } else null
    }

    override suspend fun getActualPriceByEan(eanCode: String): Either<Failure, ActualPriceInfo> {
        val scanCodeInfo = ScanCodeInfo(eanCode)
        return actualPricesRepo.getActualPriceInfoByEan(
                tkNumber = taskDescription.tkNumber,
                eanCode = scanCodeInfo.eanWithoutWeight
        ).rightToLeft(
                fnRtoL = checksForAddFunc
        )
    }

    override suspend fun getActualPriceByMatNr(matNumber: String): Either<Failure, ActualPriceInfo> {
        return actualPricesRepo.getActualPriceInfoByMatNr(
                tkNumber = taskDescription.tkNumber,
                matNumber = matNumber
        ).rightToLeft(
                fnRtoL = checksForAddFunc
        )
    }

    override fun getReportData(ip: String): CheckPriceReport {
        val notProcessedProducts = getToProcessingProducts().value ?: emptyList()
        return CheckPriceReport(
                ip = ip,
                description = taskDescription,
                isNotFinish = notProcessedProducts.isNotEmpty(),
                checksResults = getCheckResults().value ?: emptyList(),
                notProcessedResults = notProcessedProducts
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
                        matNr = item.matNr.orEmpty(),
                        name = item.name.orEmpty()
                )
            }
        }
    }

    override fun setMissing(matNrList: List<String>) {
        matNrList.forEach { material ->
            readyResultsRepo.getCheckPriceResult(material).apply {
                if (this == null) {
                    actualPricesRepo.getActualPriceInfoByMatNumber(material)?.let { actualPriceInfo ->
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
                                        userPriceInfo = null,
                                        isPrinted = false,
                                        isMissing = true
                                )
                        )
                    }
                } else {
                    readyResultsRepo.addCheckPriceResult(
                            checkPriceResult = (this).copy(isMissing = true)
                    )
                }
            }
        }
    }

    private fun isAllowedProductForTask(matNr: String): Boolean {
        if (!taskDescription.isStrictList) {
            return true
        }
        return taskDescription.additionalTaskInfo?.positions?.any { it.matNr == matNr }
                ?: true
    }

    override fun markPrinted(listOfMatNrs: List<String>) {
        listOfMatNrs.forEach {
            readyResultsRepo.getCheckPriceResult(matNr = it)?.let { priceResult ->
                readyResultsRepo.addCheckPriceResult(
                        checkPriceResult = (priceResult).copy(
                                isPrinted = true
                        )
                )
            }
        }
    }

    override fun getStateAsString(): String {
        return gson.toJson(CheckPriceData(
                taskDescription = taskDescription,
                goods = getCheckResults().value ?: emptyList()
        ))
    }

    override fun loadStateFromString(state: String) {
        val data = gson.fromJson(state, CheckPriceData::class.java)
        data.goods.map { good ->
            readyResultsRepo.addCheckPriceResult(good)
        }
    }

}

private fun CheckPriceResult.isForPriceTag(priceTagType: PriceTagType?): Boolean {
    if (priceTagType?.isRegular == null) {
        return true
    }
    return priceTagType.isRegular == actualPriceInfo.isRegular()
}

fun CheckPriceResult?.toCheckStatus(): CheckStatus? {
    return when (this?.isAllValid()) {
        true -> CheckStatus.VALID
        false -> CheckStatus.NOT_VALID
        null -> CheckStatus.ERROR
    }
}

interface ICheckPriceTask : ITask {
    fun checkProductFromVideoScan(rawCode: String?): CheckPriceResult?
    fun getCheckResults(): LiveData<List<CheckPriceResult>>
    fun getToProcessingProducts(): LiveData<List<CheckPriceResult>>
    fun removeCheckResultsByMatNumbers(matNumbers: Set<String>)
    fun getProcessingActualPrice(): ActualPriceInfo?
    fun setCheckPriceStatus(isValid: Boolean?)
    fun getCheckResultsForPrint(priceTagType: LiveData<PriceTagType>): LiveData<List<CheckPriceResult>>
    suspend fun getActualPriceByEan(eanCode: String): Either<Failure, ActualPriceInfo>
    suspend fun getActualPriceByMatNr(matNumber: String): Either<Failure, ActualPriceInfo>
    suspend fun checkPriceByQrCode(qrCode: String): Either<Failure, CheckPriceResult>
    fun getReportData(ip: String): CheckPriceReport
    fun markPrinted(listOfMatNrs: List<String>)

    var processingMatNumber: String?
}

data class CheckPriceResult(
        val ean: String,
        val matNr: String?,
        val name: String?,
        val scannedPriceInfo: ScanPriceInfo,
        val actualPriceInfo: ActualPriceInfo,
        val userPriceInfo: UserPriceInfo?,
        val isPrinted: Boolean,
        val isMissing: Boolean = false
) {

    fun isPriceValid(): Boolean? {
        if (userPriceInfo != null) {
            return userPriceInfo.isValidPrice
        }
        return scannedPriceInfo.price == actualPriceInfo.getPrice()
    }

    fun isDiscountPriceValid(): Boolean? {
        return scannedPriceInfo.discountCardPrice == actualPriceInfo.getDiscountCardPrice()
    }

    fun isAllValid(): Boolean? {
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
        val eanCode: String,
        val price: Double?,
        val discountCardPrice: Double?
)

data class ActualPriceInfo(
        val matNumber: String,
        val productName: String?,
        val price1: Double?,
        val price2: Double?,
        val price3: Double?,
        val price4: Double?,
        val options: GoodOptions
) {

    fun getPrice(): Double? {
        return price1
    }

    fun getDiscountCardPrice(): Double? {
        return when {
            price2 != null && price3 != null && price4 != null -> min(min(price2, price3), price4)
            price2 != null && price3 == null && price4 == null -> price2
            price2 != null && price3 != null -> price3
            price2 != null && price4 != null -> price4
            else -> null
        }
    }

    fun isRegular(): Boolean {
        return price3.toNullIfEmpty() == null && price4.toNullIfEmpty() == null
    }

}

data class GoodOptions(
        val matrixType: MatrixType,
        val section: String,
        val goodType: GoodType,
        val healthFood: Boolean = false,
        val novelty: Boolean = false
)

data class UserPriceInfo(override val isValidPrice: Boolean?) : IUserPriceInfo

interface IUserPriceInfo {
    /**
     * возвращает null, если нет ценника
     */
    val isValidPrice: Boolean?
}

data class CheckPriceData(
        val taskDescription: CheckPriceTaskDescription,
        val goods: List<CheckPriceResult>
)