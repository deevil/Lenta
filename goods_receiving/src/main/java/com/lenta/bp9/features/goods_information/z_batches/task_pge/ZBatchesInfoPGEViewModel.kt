package com.lenta.bp9.features.goods_information.z_batches.task_pge

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.model.processing.ProcessZBatchesPGEService
import com.lenta.bp9.model.processing.ProcessZBatchesPPPService
import com.lenta.bp9.model.task.PartySignsTypeOfZBatches
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
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

class ZBatchesInfoPGEViewModel : BaseGoodsInfo() {

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    @Inject
    lateinit var processZBatchesPGEService: ProcessZBatchesPGEService

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
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

    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }

    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
    }

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
                            ?.let { numeratorConvertBaseUnitMeasure / denominatorConvertBaseUnitMeasure }
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
    private val paramGrwOlGrundcat: MutableLiveData<String> = MutableLiveData("")
    private val paramGrwUlGrundcat: MutableLiveData<String> = MutableLiveData("")

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
                        if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
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
                        if (currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
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
                        if (processZBatchesPGEService.newProcessZBatchesPGEService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(viewModelScope = this@ZBatchesInfoPGEViewModel::viewModelScope,
                    scanResultHandler = this@ZBatchesInfoPGEViewModel::handleProductSearchResult)

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
                                ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS }
                                ?: -1
            } else {
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

            paramGrwOlGrundcat.value = dataBase.getParamGrwOlGrundcat() ?: ""
            paramGrwUlGrundcat.value = dataBase.getParamGrwUlGrundcat() ?: ""

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

    private fun convertEizToBei() : Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
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
        //todo временно закомичено, т.к. по Добавлению товара, который не числится в задании и не пересчетной ГЕ будет дорабатываться позже
        /**if (isGoodsAddedAsSurplus.value == true) { //GRZ. ПГЕ. Добавление товара, который не числится в задании https://trello.com/c/im9rJqrU
            processGeneralProductService.setProcessingUnitNumber(enteredProcessingUnitNumber.value!!)
            processGeneralProductService.add(convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, enteredProcessingUnitNumber.value!!)
            clickBtnApply()
        } else if (isNotRecountCargoUnit.value == true) { //не пересчетная ГЕ
            if ((convertEizToBei() +
                            acceptTotalCount.value!! +
                            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)) <= productInfo.value!!.orderQuantity.toDouble()) {
                processGeneralProductService.addNotRecountPGE(acceptTotalCount.value.toString(), convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                clickBtnApply()
            } else {
                screenNavigator.openAlertUnableSaveNegativeQuantity()
            }*/
        //} else {
            addPerishablePGE()
        //}
    }

    //Z-партии скоропорт расчитываются как и ПГЕ(обычный товар)-скоропорт. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.2
    private fun addPerishablePGE() {
        //блок 7.103
        if (currentQualityInfoCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
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
        if (spinEnteredDateSelectedPosition.value == infoForSpinEnteredDate.value?.indexOfLast {it.code == TERM_CONTROL_CODE_SHELF_LIFE}) {
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
                && currentTypeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            //блок 7.168
            screenNavigator.openShelfLifeExpiredDialog(
                    //блок 7.180
                    yesCallbackFunc = {
                        //блок 7.183
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"} //устанавливаем брак складской, Маша Стоян
                        //spinShelfLifeSelectedPosition.value = shelfLifeInfo.value!!.indexOfLast {it.code == "001"} закомичено, т.к. данное поле активно только при категориях Норма и Излишек
                    }
            )
            return
        }

        //блоки 7.167 и 7.190
        if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
            //блок 7.203
            addOrdinaryGoodsPGE()
            return
        }

        //блок 7.194
        screenNavigator.openShelfLifeExpiresDialog(
                //блок 7.200
                noCallbackFunc = {
                    //блок 7.201
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"} //устанавливаем брак складской
                },
                //блок 7.199
                yesCallbackFunc = {
                    //блок 7.203
                    addOrdinaryGoodsPGE()
                },
                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
        )
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
        if (processZBatchesPGEService.checkParam(paramGrwUlGrundcat.value!!)) {//блок 7.55 (да)
            //блок 7.96
            val countWithoutParamGrwUlGrundcat = processZBatchesPGEService.countWithoutParamGrwUlGrundcatPGE(paramGrwOlGrundcat.value!!, paramGrwUlGrundcat.value!!)
            //блок 7.135
            if (countWithoutParamGrwUlGrundcat == 0.0) {//блок 7.135 (да)
                //блок 7.133
                processZBatchesPGEService.removeDiscrepancyFromProduct(paramGrwUlGrundcat.value!!)
                //блок 7.177
                saveCategoryPGE(true)
            } else {//блок 7.135 (нет)
                //блок 7.157
                if (countWithoutParamGrwUlGrundcat > 0.0) {//блок 7.157 (да)
                    //блок 7.155
                    processZBatchesPGEService.addWithoutUnderload(
                            paramGrwUlGrundcat.value!!,
                            countWithoutParamGrwUlGrundcat.toString(),
                            spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5)
                    )
                    //блок 7.177
                    saveCategoryPGE(true)
                } else {//блок 7.157 (нет)
                    //блок 7.165
                    processZBatchesPGEService.removeDiscrepancyFromProduct(paramGrwUlGrundcat.value!!)
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
                            processZBatchesPGEService.addCountMoreCargoUnit(paramGrwOlGrundcat.value!!, convertEizToBei(), spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
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
        if (checkCategoryType && qualityInfo.value!![spinQualitySelectedPosition.value!!].code == paramGrwOlGrundcat.value) { //блок 7.177 (да)
            //блоки 7.181 и 7.185
        } else {
            //блок 7.185
            processZBatchesPGEService.add(
                    convertEizToBei().toString(),
                    qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                    spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5)
            )
        }

        //ПГЕ блок 7.188
        clickBtnApply()
    }

    private fun getShelfLifeDate(): String {
        return if (currentTermControlCode == TERM_CONTROL_CODE_PRODUCTION_DATE
                && currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
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
    }
}
