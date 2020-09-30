package com.lenta.bp9.features.goods_information.z_batches.task_ppp

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.model.processing.ProcessZBatchesPPPService
import com.lenta.bp9.model.task.PartySignsTypeOfZBatches
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER
import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.Logg
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

class ZBatchesInfoPPPViewModel : BaseGoodsInfo() {

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var processZBatchesPPPService: ProcessZBatchesPPPService

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val barcodeData: MutableLiveData<BarcodeData> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    private val termControlInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val spinEnteredDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinEnteredDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val enteredDate: MutableLiveData<String> = MutableLiveData("")
    val enteredTime: MutableLiveData<String> = MutableLiveData("")
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)

    private val currentTermControlCode: String
        get() {
            val position = spinEnteredDateSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        termControlInfo.value
                                ?.getOrNull(it)
                                ?.code
                                .orEmpty()
                    }.orEmpty()
        }

    val tvAccept: MutableLiveData<String> by lazy {
        val isEizUnit = productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code
        if (!isEizUnit) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
            val purchaseOrderUnitsName = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
            val uomName = productInfo.value?.uom?.name.orEmpty()
            val numeratorConvertBaseUnitMeasure =
                    productInfo.value
                            ?.numeratorConvertBaseUnitMeasure
                            ?: 0.0

            val denominatorConvertBaseUnitMeasure =
                    productInfo.value
                            ?.denominatorConvertBaseUnitMeasure
                            ?: 0.0

            val quantity =
                    denominatorConvertBaseUnitMeasure
                            .takeIf { it > 0.0 }
                            ?.let { numeratorConvertBaseUnitMeasure / it }
                            ?: 0.0

            MutableLiveData(context.getString(R.string.accept, "$purchaseOrderUnitsName=${quantity.toStringFormatted()} $uomName"))
        }
    }

    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val infoForSpinEnteredDate: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    private val paramGrsGrundNeg: MutableLiveData<String> = MutableLiveData("")

    val enabledApplyButton: MutableLiveData<Boolean> =
            countValue
                    .combineLatest(enteredDate)
                    .combineLatest(enteredTime)
                    .map {
                        val enteredCount = it?.first?.first ?: 0.0
                        val isEnteredTime = isVisibilityEnteredTime.value == false
                                || (isVisibilityEnteredTime.value == true && enteredTime.value.orEmpty().length == 5)

                        val isNorm = enteredDate.value.orEmpty().length == 10
                                && isEnteredTime

                        enteredCount > 0.0
                                && (isNorm || isDefect.value == true)
                    }

    val isVisibilityEnteredTime: MutableLiveData<Boolean> = MutableLiveData(false)

    private val currentManufactureCode: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.let {
                        manufacturersForZBatches
                                ?.takeIf { it.isNotEmpty() }
                                ?.findLast { it.manufactureName == currentManufactureName }
                                ?.manufactureCode
                                .orEmpty()
                    }
                    .orEmpty()
        }

    val acceptTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        val enteredCount = it?.first ?: 0.0
                        if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                            enteredCount + countAcceptOfProduct
                        } else {
                            countAcceptOfProduct
                        }
                    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val acceptTotalCountValue = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()

        acceptTotalCountValue
                .takeIf { count -> count > 0.0 }
                ?.run { "+ ${this.toStringFormatted()} $purchaseOrderUnits" }
                ?: "0 $purchaseOrderUnits"
    }

    val refusalTotalCount: MutableLiveData<Double> =
            countValue
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        val enteredCount = it?.first ?: 0.0
                        if (currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
                            enteredCount + countRefusalOfProduct
                        } else {
                            countRefusalOfProduct
                        }
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

            searchProductDelegate.init(scanResultHandler = this@ZBatchesInfoPPPViewModel::handleProductSearchResult)

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

                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy().orEmpty()
                spinQualitySelectedPosition.value =
                        qualityInfo.value
                                ?.indexOfLast { it.code == TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS }
                                ?: -1
            } else {
                //https://trello.com/c/mcEA3n84
                barcodeData.value?.let {
                    if (it.barcodeInfo.isWeight) {
                        val weightInGrams = it.barcodeInfo.weight.toDoubleOrNull() ?: 0.0
                        if (productInfo.value?.purchaseOrderUnits?.code?.toUpperCase(Locale.getDefault()) == UNIT_KG) {
                            count.value = (weightInGrams / 1000).toStringFormatted()
                        } else {
                            count.value = weightInGrams.toStringFormatted()
                        }
                    }
                }
                qualityInfo.value = dataBase.getQualityMercuryInfo().orEmpty()
            }

            spinQuality.value = qualityInfo.value?.map { it.name }

            spinManufacturers.value =
                    manufacturersForZBatches
                            ?.takeIf { it.isNotEmpty() }
                            ?.filter { it.materialNumber == productMaterialNumber }
                            ?.groupBy { it.manufactureName }
                            ?.map { it.key }
                            ?: listOf(context.getString(R.string.no_manufacturer_selection_required))

            infoForSpinEnteredDate.value = dataBase.getTermControlInfo()
            termControlInfo.value = dataBase.getTermControlInfo()
            spinEnteredDate.value = termControlInfo.value?.map { it.name }.orEmpty()

            /** Z-партии всегда скоропорт */
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

            val paramGrzPerishableHH = (dataBase.getParamGrzPerishableHH()?.toDoubleOrNull()
                    ?: 0.0) / 24
            val generalShelfLifeValue = generalShelfLife.value?.toDoubleOrNull() ?: 0.0
            isVisibilityEnteredTime.value = generalShelfLifeValue <= paramGrzPerishableHH
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun initProduct(initProductInfo: TaskProductInfo) {
        productInfo.value = initProductInfo
    }

    fun initDiscrepancy(_initDiscrepancy: Boolean) {
        isDiscrepancy.value = _initDiscrepancy
    }

    fun initBarcodeData(_initBarcodeData: BarcodeData) {
        barcodeData.value = _initBarcodeData
    }

    fun getTitle(): String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
    }

    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val date = formatterRU.parse(checkDate)
            !(checkDate != formatterRU.format(date)
                    || (date > currentDate.value && currentTermControlCode == TERM_CONTROL_CODE_PRODUCTION_DATE))
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
            !(checkTime != enteredTimeStr
                    || (enteredTime > currentTime && currentTermControlCode == TERM_CONTROL_CODE_PRODUCTION_DATE))
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
            screenNavigator.openAlertNotCorrectTime()
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
                    processZBatchesPPPService.addWithoutUnderload(
                            typeDiscrepancies = paramGrsGrundNegValue,
                            count = countWithoutParamGrsGrundNeg.toString(),
                            manufactureCode = currentManufactureCode,
                            shelfLifeDate = getShelfLifeDate(),
                            shelfLifeTime = getShelfLifeTime(),
                            productionDate = getProductionDate(),
                            partySignsType = getPartySignsType()
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
        val quantityAllCategoryAndEnteredCount = processZBatchesPPPService.getQuantityAllCategory() + enteredCount
        if (quantityAllCategoryAndEnteredCount > countOverdelivery) {//блок 6.190 (да)
            //блок 6.193
            screenNavigator.openAlertCountMoreOverdelivery()
            return
        }

        //блок 6.190 (нет)
        val productOrigQuantity = productInfo.value?.origQuantity?.toDoubleOrNull() ?: 0.0
        if (productOrigQuantity > productOrderQuantity) {
            val calculationOne = productOrigQuantity - productOrderQuantity
            val calculationTwo = productOrigQuantity - (processZBatchesPPPService.getQuantityAllCategory() + enteredCount)
            val calculation = if (calculationOne < calculationTwo) calculationOne else calculationTwo
            if (calculation > 0.0) {
                processZBatchesPPPService.add(
                        count = calculation.toString(),
                        typeDiscrepancies = TYPE_DISCREPANCIES_REASON_REJECTION_NOT_ORDER,
                        manufactureCode = currentManufactureCode,
                        shelfLifeDate = getShelfLifeDate(),
                        shelfLifeTime = getShelfLifeTime(),
                        productionDate = getProductionDate(),
                        partySignsType = getPartySignsType()
                )
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
    private fun saveCategory() {
        processZBatchesPPPService.add(
                count = count.value.orEmpty(),
                typeDiscrepancies = currentTypeDiscrepanciesCode,
                manufactureCode = currentManufactureCode,
                shelfLifeDate = getShelfLifeDate(),
                shelfLifeTime = getShelfLifeTime(),
                productionDate = getProductionDate(),
                partySignsType = getPartySignsType()
        )

        //ППП блок 6.176
        clickBtnApply()
    }

    private fun getShelfLifeDate(): String {
        try {
            return if (currentTermControlCode == TERM_CONTROL_CODE_PRODUCTION_DATE
                    && currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                val shelfLife = Calendar.getInstance()
                shelfLife.time = formatterRU.parse(enteredDate.value)
                shelfLife.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
                shelfLife.time?.let { formatterERP.format(it) }.orEmpty()
            } else {
                enteredDate.value
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { formatterERP.format(formatterRU.parse(it)) }
                        .orEmpty()
            }
        }
        catch (e: Exception) {
            Logg.e { "Get shelf life date exception: $e" }
            return ""
        }
    }

    private fun getProductionDate(): String {
        try {
            return if (currentTermControlCode == TERM_CONTROL_CODE_SHELF_LIFE
                    && currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                val productionDate = Calendar.getInstance()
                val generalShelfLifeValue = generalShelfLife.value?.toInt() ?: 0
                productionDate.time = formatterRU.parse(enteredDate.value)
                productionDate.add(Calendar.DATE, -generalShelfLifeValue)
                productionDate.time?.let { formatterERP.format(it) }.orEmpty()
            } else {
                enteredDate.value
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { formatterERP.format(formatterRU.parse(it)) }
                        .orEmpty()
            }
        }
        catch (e: Exception) {
            Logg.e { "Get production date exception: $e" }
            return ""
        }
    }

    private fun getShelfLifeTime(): String {
        return if (isVisibilityEnteredTime.value == true) {
            enteredTime.value.orEmpty().replace(":", "") + "00"
        } else {
            ""
        }
    }

    private fun getPartySignsType(): PartySignsTypeOfZBatches {
        return when (currentTermControlCode) {
            TERM_CONTROL_CODE_SHELF_LIFE -> PartySignsTypeOfZBatches.ShelfLife
            TERM_CONTROL_CODE_PRODUCTION_DATE -> PartySignsTypeOfZBatches.ProductionDate
            else -> PartySignsTypeOfZBatches.None
        }
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

    companion object {
        private const val TERM_CONTROL_CODE_SHELF_LIFE = "001"
        private const val TERM_CONTROL_CODE_PRODUCTION_DATE = "002"
        private const val UNIT_KG = "KG"
    }
}
