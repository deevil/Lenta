package com.lenta.bp9.features.goods_information.z_batches.task_ppp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessZBatchesPPPService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ZBatchesInfoPPPViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var processZBatchesPPPService: ProcessZBatchesPPPService

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinEnteredDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinEnteredDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val enteredDate: MutableLiveData<String> = MutableLiveData("")
    val enteredTime: MutableLiveData<String> = MutableLiveData("")
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    val isDefect: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(isDiscrepancy)
                    .map {
                        isDiscrepancy.value
                                ?.takeIf { !it }
                                ?.let { currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?: true
                    }

    val tvAccept: MutableLiveData<String> by lazy {
        val isEizUnit = productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code
        if (!isEizUnit) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
            val purchaseOrderUnitsName = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
            val uomName = productInfo.value?.uom?.name.orEmpty()
            val numeratorConvertBaseUnitMeasure = productInfo.value?.numeratorConvertBaseUnitMeasure
                    ?: 0.0
            val denominatorConvertBaseUnitMeasure = productInfo.value?.denominatorConvertBaseUnitMeasure
                    ?: 0.0
            val quantity =
                    denominatorConvertBaseUnitMeasure
                            .takeIf { it > 0.0 }
                            ?.let { numeratorConvertBaseUnitMeasure / denominatorConvertBaseUnitMeasure }
                            ?: 0.0
            MutableLiveData(context.getString(R.string.accept, "$purchaseOrderUnitsName=${quantity.toStringFormatted()} $uomName"))
        }
    }

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val infoForSpinEnteredDate: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    private val paramGrsGrundNeg: MutableLiveData<String> = MutableLiveData("")

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    @SuppressLint("SimpleDateFormat")
    private val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    val isVisibilityEnteredTime: MutableLiveData<Boolean> = MutableLiveData(false)

    private val currentQualityInfoCode: String
        get() {
            val position = spinQualitySelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        qualityInfo.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { it[position].code }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentReasonRejectionInfoCode: String
        get() {
            val position = spinReasonRejectionSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        reasonRejectionInfo.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { it[position].code }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentManufactureName: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        spinManufacturers.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { it[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentManufactureCode: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        repoInMemoryHolder.manufacturersForZBatches.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.findLast { it.manufactureName == currentManufactureName }
                                ?.manufactureCode
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentTypeDiscrepanciesCode: String
        get() {
            return currentQualityInfoCode
                    .takeIf { it == TYPE_DISCREPANCIES_QUALITY_NORM }
                    ?: currentReasonRejectionInfoCode
        }

    private val countAcceptOfProduct: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountAcceptOfProduct(product)
                    }
                    ?: 0.0
        }

    val acceptTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        val enteredCount = it?.first ?: 0.0
                        currentQualityInfoCode
                                .takeIf { code -> code == TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCount + countAcceptOfProduct }
                                ?: countAcceptOfProduct
                    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val acceptTotalCountValue = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()

        acceptTotalCountValue
                .takeIf { count -> count > 0.0 }
                ?.run { "+ ${this.toStringFormatted()} $purchaseOrderUnits" }
                ?: "0 $purchaseOrderUnits"
    }

    private val countRefusalOfProduct: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountRefusalOfProduct(product)
                    }
                    ?: 0.0
        }


    val refusalTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        val enteredCount = it?.first ?: 0.0
                        currentQualityInfoCode
                                .takeIf { code -> code != TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCount + countRefusalOfProduct }
                                ?: countRefusalOfProduct
                    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val refusalTotalCountValue = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()

        refusalTotalCountValue
                .takeIf { count -> count > 0.0 }
                ?.let { count -> "- ${count.toStringFormatted()} $purchaseOrderUnits" }
                ?: "0 $purchaseOrderUnits"
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processZBatchesPPPService.newProcessZBatchesPPPService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(viewModelScope = this@ZBatchesInfoPPPViewModel::viewModelScope,
                    scanResultHandler = this@ZBatchesInfoPPPViewModel::handleProductSearchResult)

            currentDate.value = timeMonitor.getServerDate()
            expirationDate.value = Calendar.getInstance()
            suffix.value = productInfo.value?.purchaseOrderUnits?.name.orEmpty()

            if (isDiscrepancy.value == true) {
                count.value =
                        taskManager
                                .getReceivingTask()
                                ?.run {
                                    taskRepository
                                            .getProductsDiscrepancies()
                                            .getCountProductNotProcessedOfProduct(productInfo.value!!)
                                            .toStringFormatted()
                                }

                qualityInfo.value = dataBase.getQualityMercuryInfoForDiscrepancy().orEmpty()
                spinQualitySelectedPosition.value =
                        qualityInfo.value
                                ?.indexOfLast { it.code == TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS }
                                ?: -1
            } else {
                qualityInfo.value = dataBase.getQualityMercuryInfo().orEmpty()
            }

            spinQuality.value = qualityInfo.value?.map { it.name }

            spinManufacturers.value =
                    repoInMemoryHolder
                            .manufacturersForZBatches.value
                            ?.takeIf { it.isNotEmpty() }
                            ?.groupBy { it.manufactureName }
                            ?.map { it.key }
                            ?: listOf(context.getString(R.string.no_manufacturer_selection_required))

            infoForSpinEnteredDate.value = dataBase.getTermControlInfo()
            spinEnteredDate.value = dataBase.getTermControlInfo()?.map { it.name }.orEmpty()

            /** Z-партии всегда скоропорт */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            val productGeneralShelfLife = productInfo.value?.generalShelfLife?.toInt() ?: 0
            val productRemainingShelfLife = productInfo.value?.remainingShelfLife?.toInt() ?: 0
            val productMhdhbDays = productInfo.value?.mhdhbDays ?: 0
            val productMhdrzDays = productInfo.value?.mhdrzDays ?: 0

            if (productGeneralShelfLife > 0 || productRemainingShelfLife > 0) { //https://trello.com/c/XSAxdgjt
                generalShelfLife.value = productGeneralShelfLife.toString()
                remainingShelfLife.value = productRemainingShelfLife.toString()
            } else {
                generalShelfLife.value = productMhdhbDays.toString()
                remainingShelfLife.value = productMhdrzDays.toString()
            }

            paramGrsGrundNeg.value = dataBase.getParamGrsGrundNeg().orEmpty()

            val paramGrzPerishableHH = dataBase.getParamGrzPerishableHH()?.toDoubleOrNull() ?: 0.0
            val generalShelfLifeValue = generalShelfLife.value?.toDoubleOrNull() ?: 0.0
            isVisibilityEnteredTime.value = true //generalShelfLifeValue <= paramGrzPerishableHH
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun initProduct(initProductInfo: TaskProductInfo) {
        productInfo.value = initProductInfo
    }

    fun initDiscrepancy(initDiscrepancy: Boolean) {
        isDiscrepancy.value = initDiscrepancy
    }

    fun getTitle(): String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
    }

    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val date = formatterRU.parse(checkDate)
            !(checkDate != formatterRU.format(date) || date > currentDate.value)
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCorrectTime(checkTime: String?): Boolean {
        return try {
            val milliseconds = timeMonitor.getUnixTime()
            val currentTimeStr = DateTimeUtil.formatDate(milliseconds, Constants.TIME_FORMAT_HHmm)
            val currentTime = SimpleDateFormat(Constants.TIME_FORMAT_HHmm).parse(currentTimeStr)
            val enteredTime = SimpleDateFormat(Constants.TIME_FORMAT_HHmm).parse(checkTime)
            val enteredTimeStr = SimpleDateFormat(Constants.TIME_FORMAT_HHmm).format(enteredTime)
            !(checkTime != enteredTimeStr || enteredTime > currentTime)
        } catch (e: Exception) {
            false
        }
    }

    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(currentQualityInfoCode)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            reasonRejectionInfo.value = dataBase.getReasonRejectionMercuryInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map { it.name }
            if (isDiscrepancy.value == true) {
                spinReasonRejectionSelectedPosition.value =
                        reasonRejectionInfo.value
                                ?.indexOfLast { it.code == "44" }
                                .let {
                                    val reasonRejectionInfoValue = it ?: 0
                                    if (reasonRejectionInfoValue < 0) {
                                        0
                                    } else {
                                        reasonRejectionInfoValue
                                    }
                                }
            } else {
                spinReasonRejectionSelectedPosition.value = 0
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinsEnteredDate(position: Int) {
        spinEnteredDateSelectedPosition.value = position
    }

    fun onClickPositionSpinRejectRejection(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onBackPressed() {
        if (enabledApplyButton.value == true) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    fun checkScanResult(data: String) {
        addGoods.value = false
        onClickAdd()
        if (addGoods.value == true) {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openGoodsDetailsScreen(it) }
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onClickAdd() {
        addPerishable()
    }

    //Z-партии скоропорт расчитываются как и ППП(обычный товар)-скоропорт. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.3
    private fun addPerishable() {
        //блок 6.101
        if (currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
            addOrdinaryGoods()
            return
        }

        if (!isCorrectDate(enteredDate.value)) {
            screenNavigator.openAlertNotCorrectDate()
            return
        }

        if (!isCorrectTime(enteredTime.value) && isVisibilityEnteredTime.value == true) {
            screenNavigator.openAlertNotCorrectDate()
            return
        }

        //блок 6.131
        if (spinEnteredDateSelectedPosition.value == infoForSpinEnteredDate.value?.indexOfLast { it.code == "001" }) {
            //блок 6.146
            expirationDate.value?.time = formatterRU.parse(enteredDate.value)
        } else {
            //блок 6.144
            expirationDate.value?.time = formatterRU.parse(enteredDate.value)
            expirationDate.value?.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
        }


        if (expirationDate.value!!.time <= currentDate.value
                && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            //блок 6.158
            screenNavigator.openShelfLifeExpiredDialog(
                    //блок 6.170
                    yesCallbackFunc = {
                        //блок 6.174
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "7" }
                    }
            )
            return
        }

        //блоки 6.157 и 6.182
        if (Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0) {
            //блок 6.192
            addOrdinaryGoods()
            return
        }

        //блок 6.184
        screenNavigator.openShelfLifeExpiresDialog(
                //блок 6.189
                noCallbackFunc = {
                    //блок 6.191
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "7" }
                },
                //блок 6.188
                yesCallbackFunc = {
                    //блок 6.192
                    addOrdinaryGoods()
                },
                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
        )
    }

    //как и ППП-обычный товар. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.8
    private fun addOrdinaryGoods() {
        val enteredCount = countValue.value ?: 0.0
        //блок 6.16
        if (processZBatchesPPPService.countEqualOrigQuantity(enteredCount)) {//блок 6.16 (да)
            //блок 6.172
            saveCategory()
        } else {//блок 6.16 (нет)
            //блок 6.22
            if (processZBatchesPPPService.countMoreOrigQuantity(enteredCount)) {//блок 6.22 (да)
                //блок 6.58
                checkParamGrsGrundNeg()
            } else {//блок 6.22 (нет)
                //блок 6.26
                if (productInfo.value!!.uom.code == "G") {//блок 6.26 (да)
                    //блок 6.49
                    val roundingQuantity = processZBatchesPPPService.getRoundingQuantity() - enteredCount // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при отработке карточки https://trello.com/c/hElr3cn3
                    //блок 6.90
                    val productRoundingShortages = productInfo.value?.roundingShortages?.toDoubleOrNull()
                            ?: 0.0
                    if (roundingQuantity <= productRoundingShortages) {//блок 6.90 (да)
                        //блок 6.109
                        screenNavigator.openRoundingIssueDialog(
                                //блок 6.148
                                noCallbackFunc = {
                                    //блок 6.172
                                    saveCategory()
                                },
                                //блок 6.149
                                yesCallbackFunc = {
                                    //блок 6.154
                                    count.value = (enteredCount + roundingQuantity).toString()
                                    //блок 6.172
                                    saveCategory()
                                }
                        )
                    } else {//блок 6.90 (нет)
                        //блок 6.172
                        saveCategory()
                    }
                } else {//блок 6.26 (нет)
                    //блок 6.172
                    saveCategory()
                }
            }
        }
    }

    //ППП блок 6.58
    private fun checkParamGrsGrundNeg() {
        val paramGrsGrundNegValue = paramGrsGrundNeg.value.orEmpty()
        if (processZBatchesPPPService.checkParam(paramGrsGrundNegValue)) {//блок 6.58 (да)
            //блок 6.93
            val countWithoutParamGrsGrundNeg = processZBatchesPPPService.countWithoutParamGrsGrundNeg(paramGrsGrundNegValue)
            //блок 6.130
            if (countWithoutParamGrsGrundNeg == 0.0) {//блок 6.130 (да)
                //блок 6.121
                processZBatchesPPPService.removeDiscrepancyFromProduct(paramGrsGrundNegValue)
                //блок 6.172
                saveCategory()
            } else {//блок 6.130 (нет)
                //блок 6.147
                if (countWithoutParamGrsGrundNeg > 0.0) {//блок 6.147 (да)
                    //блок 6.145
                    val shelfLifeDate = formatterERP.format(formatterRU.parse(enteredDate.value.orEmpty()))
                    val shelfLifeTime = enteredTime.value.orEmpty().replace(":", "") + "00"
                    processZBatchesPPPService.addWithoutUnderload(
                            typeDiscrepancies = paramGrsGrundNegValue,
                            count = countWithoutParamGrsGrundNeg.toString(),
                            manufactureCode = currentManufactureCode,
                            shelfLifeDate = shelfLifeDate,
                            shelfLifeTime = shelfLifeTime
                    )
                    //блок 6.172
                    saveCategory()
                } else {//блок 6.147 (нет)
                    //блок 6.155
                    processZBatchesPPPService.removeDiscrepancyFromProduct(paramGrsGrundNegValue)
                    noParamGrsGrundNeg()
                }
            }
        } else {//блок 6.58 (нет)
            noParamGrsGrundNeg()
        }
    }

    //ППП блок 6.163
    private fun noParamGrsGrundNeg() {
        if (productInfo.value?.uom?.code == "G") {//блок 6.163 (да)
            //блок 6.167
            val enteredCount = countValue.value ?: 0.0
            val roundingQuantity = processZBatchesPPPService.getRoundingQuantity() - enteredCount // "- enteredCount" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
            //блок 6.173
            val productRoundingSurplus = productInfo.value?.roundingSurplus?.toDoubleOrNull() ?: 0.0
            if (roundingQuantity <= productRoundingSurplus) {//блок 6.173 (да)
                //блок 6.175
                screenNavigator.openRoundingIssueDialog(
                        //блок 6.178
                        noCallbackFunc = {
                            //блок 6.187
                            calculationOverdelivery()
                        },
                        //блок 6.179
                        yesCallbackFunc = {
                            //блок 6.185
                            count.value = (enteredCount + roundingQuantity).toString()
                            //блок 6.172
                            saveCategory()
                        }
                )
            } else {//блок 6.173 (нет)
                //блок 6.187
                calculationOverdelivery()
            }
        } else {//блок 6.163 (нет)
            //блок 6.187
            calculationOverdelivery()
        }
    }

    //ППП блок 6.187
    private fun calculationOverdelivery() {
        val enteredCount = countValue.value ?: 0.0
        //блок 6.187
        val productOrderQuantity = productInfo.value?.orderQuantity?.toDoubleOrNull() ?: 0.0
        val productOverdToleranceLimit = productInfo.value?.overdToleranceLimit?.toDoubleOrNull()
                ?: 0.0
        val countOverdelivery = productOrderQuantity + (productOverdToleranceLimit / 100) * productOrderQuantity

        //блок 6.190
        if (processZBatchesPPPService.getQuantityAllCategory(enteredCount) > countOverdelivery) {//блок 6.190 (да)
            //блок 6.193
            screenNavigator.openAlertCountMoreOverdelivery()
            return
        }

        //блок 6.190 (нет)
        val productOrigQuantity = productInfo.value?.origQuantity?.toDoubleOrNull() ?: 0.0
        if (productOrigQuantity > productOrderQuantity) {
            val calculationOne = productOrigQuantity - productOrderQuantity
            val calculationTwo = productOrigQuantity - processZBatchesPPPService.getQuantityAllCategory(enteredCount)
            val calculation = if (calculationOne < calculationTwo) calculationOne else calculationTwo
            if (calculation > 0.0) {
                val shelfLifeDate = formatterERP.format(formatterRU.parse(enteredDate.value.orEmpty()))
                val shelfLifeTime = enteredTime.value.orEmpty().replace(":", "") + "00"
                processZBatchesPPPService.add(calculation.toString(), "41", currentManufactureCode, shelfLifeDate, shelfLifeTime)
            }
        }

        //блок 6.196
        if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            saveCategory()
        } else {
            if (processZBatchesPPPService.categNormNotOrderMoreOrigQuantity()) {
                screenNavigator.openAlertCountMoreOverdelivery()
            } else {
                saveCategory()
            }
        }
    }

    //ППП блок 6.172
    @SuppressLint("SimpleDateFormat")
    private fun saveCategory() {
        val countAdd = if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) acceptTotalCount.value.toString() else count.value
        val shelfLifeDate = formatterERP.format(formatterRU.parse(enteredDate.value.orEmpty()))
        val shelfLifeTime = enteredTime.value.orEmpty().replace(":", "") + "00"
        processZBatchesPPPService.add(countAdd.orEmpty(), TYPE_DISCREPANCIES_QUALITY_NORM, currentManufactureCode, shelfLifeDate, shelfLifeTime)

        //ППП блок 6.176
        clickBtnApply()
    }

    //ППП блок 6.176 и ПГЕ блок 7.188
    private fun clickBtnApply() {
        addGoods.value = true
        if (isClickApply.value == true) {
            screenNavigator.goBack()
        } else {
            count.value = "0"
        }
    }

}
