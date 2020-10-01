package com.lenta.bp9.features.goods_information.z_batches.task_pge

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.model.processing.ProcessZBatchesPGEService
import com.lenta.bp9.model.task.PartySignsTypeOfZBatches
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.shared.fmp.resources.dao_ext.getEanInfoByMaterialUnits
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ZBatchesInfoPGEViewModel : BaseGoodsInfo() {

    @Inject
    lateinit var processZBatchesPGEService: ProcessZBatchesPGEService

    @Inject
    lateinit var hyperHive: HyperHive

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinTermControl: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val enteredDate: MutableLiveData<String> = MutableLiveData("")
    val enteredTime: MutableLiveData<String> = MutableLiveData("")
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val tvGeneralShelfLife: MutableLiveData<String> = MutableLiveData("")
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val tvRemainingShelfLife: MutableLiveData<String> = MutableLiveData("")
    val tvAlternativeUnitMeasure: MutableLiveData<String> = MutableLiveData()
    val isVisibilityEnteredTime: MutableLiveData<Boolean> = MutableLiveData(false)
    val isShelfLifeObtainedFromEWM: MutableLiveData<Boolean> = MutableLiveData(false)

    val tvAccept: MutableLiveData<String> by lazy {
        if (isOrderUnitAndBaseUnitDifferent.value == false) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
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
                            ?.let { numeratorConvertBaseUnitMeasure / denominatorConvertBaseUnitMeasure }
                            ?: 0.0

            MutableLiveData(context.getString(R.string.accept, "$orderUnitName=${quantity.toStringFormatted()} $baseUnitName"))
        }
    }

    private val quantityByZBatchWithProductionDate: MutableLiveData<String> =
            spinManufacturersSelectedPosition
                    .combineLatest(spinProductionDateSelectedPosition)
                    .map {
                        val zBatchInfo = getZBatchInfo()
                        val zBatchQuantityBaseUnit = zBatchInfo?.purchaseOrderScope.toStringFormatted()
                        val quantityAlternativeUnitMeasure = zBatchInfo?.quantityAlternativeUnitMeasure.toStringFormatted()
                        val alternativeUnitMeasure = ZmpUtz07V001(hyperHive).getUnitName(zBatchInfo?.alternativeUnitMeasure.orEmpty()).orEmpty()
                        buildString {
                            append(zBatchQuantityBaseUnit)
                            append(" ")
                            append(baseUnitName)
                            append("/")
                            append(quantityAlternativeUnitMeasure)
                            append(" ")
                            append(alternativeUnitMeasure)
                        }
                    }

    private fun getZBatchInfo(): TaskZBatchInfo? {
        return try {
            taskZBatchesInfo
                    ?.findLast {
                        it.materialNumber == productMaterialNumber
                                && it.processingUnit == productInfo.value?.processingUnit
                                && it.manufactureCode == currentManufactureCode
                                && it.productionDate == formatterEN.format(formatterRU.parse(currentProductionDate))
                    }
        } catch (e: Exception) {
            Logg.e { "ZBatchesInfoPGEViewModel -> getZBatchInfo: $e" }
            null
        }
    }

    val tvProductionDate: MutableLiveData<String> = quantityByZBatchWithProductionDate.map {
        if (isGoodsAddedAsSurplus.value == true) {
            context.getString(R.string.zbatch_with_production_date)
        } else {
            context.getString(R.string.zbatch_quantity_with_production_date, it.orEmpty())
        }
    }

    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    private val paramGrwOlGrundcat: MutableLiveData<String> = MutableLiveData("")
    private val paramGrwUlGrundcat: MutableLiveData<String> = MutableLiveData("")

    val isDisabledButtonUnitType: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        if (isSelectedOrderUnit.value == true
                && currentTypeDiscrepanciesCodeByTaskType.isNotEmpty()
                && currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_NORM) {
            onClickUnitChange()
        }

        currentTypeDiscrepanciesCodeByTaskType != TYPE_DISCREPANCIES_QUALITY_NORM
    }

    val enabledApplyButton: MutableLiveData<Boolean> =
            countValue
                    .combineLatest(enteredDate)
                    .combineLatest(enteredTime)
                    .map {
                        val enteredCount = it?.first?.first ?: 0.0
                        val isEnteredTime = isVisibilityEnteredTime.value == false
                                || (isVisibilityEnteredTime.value == true && enteredTime.value.orEmpty().length == 5)

                        val isCheckTermControl = enteredDate.value.orEmpty().length == 10
                                && isEnteredTime

                        enteredCount > 0.0
                                && (isCheckTermControl || isDefect.value == true || isShelfLifeObtainedFromEWM.value == true)
                    }

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

    override val spinProductionDate: MutableLiveData<List<String>> =
            spinManufacturersSelectedPosition
                    .map {
                        taskZBatchesInfo
                                ?.filter { batch ->
                                    batch.materialNumber == productMaterialNumber
                                            && batch.processingUnit == productInfo.value?.processingUnit
                                            && batch.manufactureCode == currentManufactureCode
                                }
                                ?.groupBy { it.productionDate }
                                ?.map {
                                    try {
                                        formatterRU.format(formatterEN.parse(it.key))
                                    } catch (e: Exception) {
                                        Logg.e { "e: $e" }
                                        ""
                                    }
                                }
                                .orEmpty()
                    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processZBatchesPGEService.newProcessZBatchesPGEService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@ZBatchesInfoPGEViewModel::handleProductSearchResult)

            processingUnitsOfProduct.value = getProcessingUnitsOfProduct(productMaterialNumber)
            val processingUnits = processingUnitsOfProduct.value?.map { it.processingUnit }.orEmpty()
            isShelfLifeObtainedFromEWM.value =
                    taskZBatchesInfo
                            ?.findLast {
                                it.materialNumber == productMaterialNumber
                                        && processingUnits.all { processingUnit -> processingUnit == it.processingUnit} //todo так было ранее и вроде так было не верно && it.processingUnit == productInfo.value?.processingUnit
                            }
                            ?.shelfLifeDate
                            ?.isNotEmpty()
                            ?: false

            paramGrwOlGrundcat.value = dataBase.getParamGrwOlGrundcat().orEmpty()
            paramGrwUlGrundcat.value = dataBase.getParamGrwUlGrundcat().orEmpty()

            when {
                isGoodsAddedAsSurplus.value == true -> { //товар, который не числится в задании
                    spinProcessingUnit.value = listOf(productInfo.value?.processingUnit.orEmpty())
                    suffix.value = unitNameByTaskType
                    qualityInfo.value = dataBase.getSurplusInfoForZBatchesTaskPGE().orEmpty()
                }
                isDiscrepancy.value == true -> {
                    suffix.value = unitNameByTaskType
                    count.value = getQuantityByProductForMarriageOfProcessingUnits()


                    if (isNotRecountCargoUnit.value == true) {
                        qualityInfo.value = dataBase.getQualityInfoZBatchesTaskPGENotRecountBreaking().orEmpty()
                    } else {
                        qualityInfo.value = dataBase.getQualityInfoZBatchesTaskPGEForDiscrepancy().orEmpty()
                    }
                }
                else -> {
                    suffix.value = orderUnitName
                    if (isNotRecountCargoUnit.value == true) {
                        qualityInfo.value = dataBase.getQualityInfoZBatchesTaskPGENotRecountBreaking().orEmpty()
                    } else {
                        qualityInfo.value = dataBase.getQualityInfoZBatchesTaskPGE().orEmpty()
                    }
                }
            }

            currentDate.value = timeMonitor.getServerDate()
            expirationDate.value = Calendar.getInstance()
            spinQuality.value = qualityInfo.value?.map { it.name }

            spinManufacturers.value =
                    manufacturersForZBatches
                            ?.takeIf { it.isNotEmpty() }
                            ?.filter { it.materialNumber == productMaterialNumber }
                            ?.groupBy { it.manufactureName }
                            ?.map { it.key }
                            ?: listOf(context.getString(R.string.no_manufacturer_selection_required))

            termControlType.value = dataBase.getTermControlInfo()
            spinTermControl.value = termControlType.value?.map { it.name }.orEmpty()

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

            tvGeneralShelfLife.value = "${context.getString(R.string.general_shelf_life_abbr)}: ${generalShelfLife.value} ${context.getString(R.string.day_abbreviated)}"
            tvRemainingShelfLife.value = "${context.getString(R.string.remaining_shelf_life_abbr)}: ${remainingShelfLife.value} ${context.getString(R.string.day_abbreviated)}"

            tvAlternativeUnitMeasure.value = buildString {
                append(context.getString(R.string.alternative_unit_measure_abbr))
                append(": ")
                append(productInfo.value?.quantityAlternativeUnitMeasure?.toStringFormatted())
                append(productInfo.value?.alternativeUnitMeasure.orEmpty())
            }

            val paramGrzPerishableHH = (dataBase.getParamGrzPerishableHH()?.toDoubleOrNull()
                    ?: 0.0) / 24
            val generalShelfLifeValue = generalShelfLife.value?.toDoubleOrNull() ?: 0.0
            isVisibilityEnteredTime.value = generalShelfLifeValue <= paramGrzPerishableHH
        }
    }

    private fun getQuantityByProductForMarriageOfProcessingUnits(): String {
        val countProcessingUnitsOfProduct = processingUnitsOfProduct.value?.size ?: 0

        return if (countProcessingUnitsOfProduct > 1) { //если у товара две ЕО
            val countOrderQuantity =
                    processingUnitsOfProduct.value
                            ?.map { it.orderQuantity.toDouble() }
                            ?.sumByDouble { it }
                            ?: 0.0

            productInfo.value
                    ?.let { getCountProductNotProcessedOfProductPGEOfProcessingUnits(it, countOrderQuantity).toStringFormatted() }
                    .orEmpty()

        } else {
            productInfo.value
                    ?.let { getCountProductNotProcessedOfProductPGE(it).toStringFormatted() }
                    .orEmpty()
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
        spinQualitySelectedPosition.value = position
        updateDataSpinReasonRejection()
    }

    private fun updateDataSpinReasonRejection() {
        spinProcessingUnitSelectedPosition.value = 0
        spinProcessingUnit.value = processingUnitsOfProduct.value?.map { "ЕО - " + it.processingUnit }
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinTermControl(position: Int) {
        spinTermControlSelectedPosition.value = position
    }

    fun onClickPositionSpinProcessingUnit(position: Int) {
        spinProcessingUnitSelectedPosition.value = position

        productInfo.value = processingUnitsOfProduct.value?.getOrNull(position)
        val processingUnits = processingUnitsOfProduct.value?.map { it.processingUnit }.orEmpty()
        isShelfLifeObtainedFromEWM.value =
                taskZBatchesInfo
                        ?.findLast {
                            it.materialNumber == productMaterialNumber
                                    && processingUnits.all { processingUnit -> processingUnit == it.processingUnit}
                        }
                        ?.shelfLifeDate
                        ?.isNotEmpty()
                        ?: false

        productInfo.value
                ?.let {
                    if (processZBatchesPGEService.newProcessZBatchesPGEService(it) == null) {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                    }
                }
                ?.orIfNull {
                    screenNavigator.goBackAndShowAlertWrongProductType()
                }
    }

    fun onClickPositionSpinProductionDate(position: Int) {
        spinProductionDateSelectedPosition.value = position
    }

    fun onClickUnitChange() {
        isSelectedOrderUnit.value = isSelectedOrderUnit.value?.let { !it } ?: true
        suffix.value = if (isSelectedOrderUnit.value == true) {
            orderUnitName
        } else {
            baseUnitName
        }
        count.value = count.value //чтобы обновилась переменная countValue (зависит от count)  и соответственно данные в поле Принять/Отказать
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
        //todo временно закомичено, т.к. по Добавлению товара, который не числится в задании и не пересчетной ГЕ будет дорабатываться позже
        if (isGoodsAddedAsSurplus.value == true) { //GRZ. ПГЕ. Добавление товара, который не числится в задании
            //todo processZBatchesPGEService.setProcessingUnitNumber(enteredProcessingUnitNumber.value!!)
            //todo processZBatchesPGEService.add(convertEizToBei().toString(), currentTypeDiscrepanciesCodeByTaskType, enteredProcessingUnitNumber.value!!)
            //todo clickBtnApply()
        } else if (isNotRecountCargoUnit.value == true) { //не пересчетная ГЕ
            val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
            val totalCount = convertEizToBei() + acceptTotalCountValue + countRefusalOfProductByTaskType
            val productOrderQuantity = productInfo.value?.orderQuantity?.toDoubleOrNull() ?: 0.0
            if (totalCount <= productOrderQuantity) {
                //todo processZBatchesPGEService.addNotRecountPGE(acceptTotalCount.value.toString(), convertEizToBei().toString(), currentTypeDiscrepanciesCodeByTaskType, currentProcessingUnitNumber)
                //todo clickBtnApply()
            } else {
                screenNavigator.openAlertUnableSaveNegativeQuantity()
            }
        } else {
            addPerishablePGE()
        }
    }

    //Z-партии скоропорт расчитываются как и ПГЕ(обычный товар)-скоропорт. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.2
    private fun addPerishablePGE() {
        try {
            //блок 7.103
            if (currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM || isShelfLifeObtainedFromEWM.value == true) {
                addOrdinaryGoodsPGE()
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

            //блок 7.134
            if (spinTermControlSelectedPosition.value == termControlType.value?.indexOfLast { it.code == TERM_CONTROL_CODE_SHELF_LIFE }) {
                //блок 7.154
                expirationDate.value?.time = formatterRU.parse(enteredDate.value)
            } else {
                //блок 7.153
                expirationDate.value?.time = formatterRU.parse(enteredDate.value)
                expirationDate.value?.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
            }

            //блок 7.160
            val currentTypeDiscrepancies =
                    qualityInfo.value
                            ?.get(spinQualitySelectedPosition.value ?: 0)
                            ?.code
                            .orEmpty()
            if (expirationDate.value!!.time <= currentDate.value
                    && currentTypeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
                //блок 7.168
                screenNavigator.openShelfLifeExpiredDialog(
                        //блок 7.180
                        yesCallbackFunc = {
                            //блок 7.183
                            spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "5" } //устанавливаем брак складской, Маша Стоян
                        }
                )
                return
            }

            //блоки 7.167 и 7.190
            if (Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0) {
                //блок 7.203
                addOrdinaryGoodsPGE()
                return
            }

            //блок 7.194
            screenNavigator.openShelfLifeExpiresDialog(
                    //блок 7.200
                    noCallbackFunc = {
                        //блок 7.201
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "5" } //устанавливаем брак складской
                    },
                    //блок 7.199
                    yesCallbackFunc = {
                        //блок 7.203
                        addOrdinaryGoodsPGE()
                    },
                    expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
            )
        } catch (e: Exception) {
            Logg.e { "Z-batch fun addPerishablePGE: $e" }
        }
    }

    //как и в ПГЕ-обычный товар. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.6
    private fun addOrdinaryGoodsPGE() {
        //блок 7.15
        if (processZBatchesPGEService.countEqualOrigQuantityPGE(convertEizToBei())) {//блок 7.15 (да)
            //блок 7.177
            saveCategoryPGE(true)
        } else {//блок 7.15 (нет)
            //блок 7.21 (processGeneralProductService.getOpenQuantityPGE - блок 7.11)
            if (processZBatchesPGEService.getQuantityAllCategoryPGE(convertEizToBei()) > processZBatchesPGEService.getOpenQuantityPGE(paramGrwOlGrundcat.value!!, paramGrwUlGrundcat.value!!)) {
                //блок 7.55
                checkParamGrwUlGrundcat()
            } else {
                //блок 7.43
                if (productInfo.value!!.uom.code == "G") {
                    //блок 7.63
                    val roundingQuantity = processZBatchesPGEService.getRoundingQuantityPGE() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
                    //блок 7.110
                    if (roundingQuantity <= productInfo.value!!.roundingShortages.toDouble()) {//блок 7.110 (да)
                        //блок 7.156
                        screenNavigator.openRoundingIssueDialog(
                                //блок 7.163
                                noCallbackFunc = {
                                    //блок 7.177
                                    saveCategoryPGE(true)
                                },
                                //блок 7.164
                                yesCallbackFunc = {
                                    //блок 7.169
                                    count.value = (countValue.value!! + roundingQuantity).toString()
                                    //блок 7.177
                                    saveCategoryPGE(true)
                                }
                        )
                    } else {//блок 7.110 (нет)
                        //блок 7.177
                        saveCategoryPGE(true)
                    }
                } else {
                    //блок 7.177
                    saveCategoryPGE(true)
                }
            }
        }
    }

    //ПГЕ блок 7.55
    private fun checkParamGrwUlGrundcat() {
        val paramGrwUlGrundcatValue = paramGrwUlGrundcat.value.orEmpty()
        if (processZBatchesPGEService.checkParam(paramGrwUlGrundcat.value.orEmpty())) {//блок 7.55 (да)
            //блок 7.96
            val countWithoutParamGrwUlGrundcat = processZBatchesPGEService.countWithoutParamGrwUlGrundcatPGE(paramGrwOlGrundcat.value.orEmpty(), paramGrwUlGrundcat.value.orEmpty())
            //блок 7.135
            if (countWithoutParamGrwUlGrundcat == 0.0) {//блок 7.135 (да)
                //блок 7.133
                processZBatchesPGEService.removeDiscrepancyFromProduct(paramGrwUlGrundcat.value.orEmpty(), currentProcessingUnitNumber)
                //блок 7.177
                saveCategoryPGE(true)
            } else {//блок 7.135 (нет)
                //блок 7.157
                if (countWithoutParamGrwUlGrundcat > 0.0) {//блок 7.157 (да)
                    //блок 7.155
                    processZBatchesPGEService.addWithoutUnderload(
                            typeDiscrepancies = paramGrwUlGrundcatValue,
                            count = countWithoutParamGrwUlGrundcat.toString(),
                            manufactureCode = currentManufactureCode,
                            shelfLifeDate = getShelfLifeDate(),
                            shelfLifeTime = getShelfLifeTime(),
                            productionDate = getProductionDate(),
                            processingUnit = currentProcessingUnitNumber,
                            partySignsType = getPartySignsType(),
                            isShelfLifeObtainedFromEWM = isShelfLifeObtainedFromEWM.value ?: false
                    )
                    //блок 7.177
                    saveCategoryPGE(true)
                } else {//блок 7.157 (нет)
                    //блок 7.165
                    processZBatchesPGEService.removeDiscrepancyFromProduct(paramGrwUlGrundcatValue, currentProcessingUnitNumber)
                    noParamGrwUlGrundcat()
                }
            }
        } else {//блок 7.55 (нет)
            noParamGrwUlGrundcat()
        }
    }

    //ПГЕ блок 7.174
    private fun noParamGrwUlGrundcat() {
        if (productInfo.value!!.uom.code == "G") {//блок 7.174 (да)
            //блок 7.178
            val roundingQuantity = processZBatchesPGEService.getRoundingQuantityPGE() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
            //блок 7.184
            if (roundingQuantity <= productInfo.value!!.roundingSurplus.toDouble()) {//блок 7.184 (да)
                //блок 7.186
                screenNavigator.openRoundingIssueDialog(
                        //блок 7.195
                        noCallbackFunc = {
                            //блок 7.198
                            checkParamGrwOlGrundcat()
                        },
                        //блок 7.193
                        yesCallbackFunc = {
                            //блок 7.196
                            count.value = (countValue.value!! + roundingQuantity).toString()
                            //блок 7.177
                            saveCategoryPGE(true)
                        }
                )
            } else {//блок 7.184 (нет)
                //блок  7.198
                checkParamGrwOlGrundcat()
            }
        } else {//блок 7.174 (нет)
            //блок 7.198
            checkParamGrwOlGrundcat()
        }
    }

    //ПГЕ блок 7.198
    private fun checkParamGrwOlGrundcat() {
        if (qualityInfo.value!![spinQualitySelectedPosition.value!!].code == "1" || qualityInfo.value!![spinQualitySelectedPosition.value!!].code == paramGrwOlGrundcat.value!!) {//блок 7.198 (да)
            //блок 7.202
            val countNormAndParamMoreOrderQuantity = processZBatchesPGEService.countNormAndParamMoreOrderQuantityPGE(paramGrwOlGrundcat.value!!, convertEizToBei())
            if (countNormAndParamMoreOrderQuantity) {//блок 7.202 (да)
                //блок 7.205
                screenNavigator.openAlertCountMoreCargoUnitDialog(
                        //блок 7.208
                        yesCallbackFunc = {
                            //блок 7.209
                            processZBatchesPGEService.addCountMoreCargoUnit(
                                    paramGrwOlGrundcat = paramGrwOlGrundcat.value.orEmpty(),
                                    count = convertEizToBei(),
                                    processingUnit = currentProcessingUnitNumber,
                                    manufactureCode = currentManufactureCode,
                                    shelfLifeDate = getShelfLifeDate(),
                                    shelfLifeTime = getShelfLifeTime(),
                                    productionDate = getProductionDate(),
                                    partySignsType = getPartySignsType(),
                                    isShelfLifeObtainedFromEWM = isShelfLifeObtainedFromEWM.value ?: false
                            )
                            //блок 7.188 (переходим минуя 7.177 и 7.185, т.к. мы уже сохранили данные в блоке 7.209)
                            clickBtnApply()
                        }
                )
            } else {//блок 7.202 (нет)
                //блок 7.185
                saveCategoryPGE(false)
            }
        } else {//блок 7.198 (нет)
            //блок 7.185
            saveCategoryPGE(false)
        }
    }

    //ПГЕ блоки 7.177 и 7.185
    private fun saveCategoryPGE(checkCategoryType: Boolean) {
        //если checkCategoryType==true, значит перед сохранением (блок 7.185) делаем блок 7.177
        if (checkCategoryType && currentTypeDiscrepanciesCodeByTaskType == paramGrwOlGrundcat.value) { //блок 7.177 (да)
            //блоки 7.181 и 7.185
        } else {
            //блок 7.185
            processZBatchesPGEService.add(
                    count = convertEizToBei().toString(),
                    typeDiscrepancies = currentTypeDiscrepanciesCodeByTaskType,
                    manufactureCode = currentManufactureCode,
                    shelfLifeDate = getShelfLifeDate(),
                    shelfLifeTime = getShelfLifeTime(),
                    productionDate = getProductionDate(),
                    processingUnit = currentProcessingUnitNumber,
                    partySignsType = getPartySignsType(),
                    isShelfLifeObtainedFromEWM = isShelfLifeObtainedFromEWM.value ?: false
            )
        }

        //ПГЕ блок 7.188
        clickBtnApply()
    }

    private fun getShelfLifeDate(): String {
        return if (isShelfLifeObtainedFromEWM.value == true) {
            getShelfLifeWhenObtainedFromEWM()
        } else {
            getShelfLifeWhenNotObtainedFromEWM()
        }
    }

    private fun getShelfLifeWhenObtainedFromEWM(): String {
        return try {
            val shelfLife = Calendar.getInstance()
            shelfLife.time = formatterRU.parse(currentProductionDate)
            shelfLife.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
            shelfLife.time?.let { formatterERP.format(it) }.orEmpty()
        } catch (e: Exception) {
            Logg.e { "Get shelf life date when obtained from EWM exception: $e" }
            ""
        }
    }

    private fun getShelfLifeWhenNotObtainedFromEWM(): String {
        return try {
            if (currentTermControlCode == TERM_CONTROL_CODE_PRODUCTION_DATE
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
        } catch (e: Exception) {
            Logg.e { "Get shelf life date exception: $e" }
            ""
        }
    }

    private fun getProductionDate(): String {
        return if (isShelfLifeObtainedFromEWM.value == true) {
            getProductionDateWhenObtainedFromEWM()
        } else {
            getProductionDateWhenNotObtainedFromEWM()
        }
    }

    private fun getProductionDateWhenObtainedFromEWM(): String {
        return try {
            formatterERP.format(formatterRU.parse(currentProductionDate))
        } catch (e: Exception) {
            Logg.e { "Get shelf life date when obtained from EWM exception: $e" }
            ""
        }
    }

    private fun getProductionDateWhenNotObtainedFromEWM(): String {
        return try {
            if (currentTermControlCode == TERM_CONTROL_CODE_SHELF_LIFE
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
        } catch (e: Exception) {
            Logg.e { "Get production date exception: $e" }
            ""
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
            else -> {
                if (isShelfLifeObtainedFromEWM.value == true) {
                    PartySignsTypeOfZBatches.ProductionDate
                } else {
                    PartySignsTypeOfZBatches.None
                }
            }
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
    }
}
