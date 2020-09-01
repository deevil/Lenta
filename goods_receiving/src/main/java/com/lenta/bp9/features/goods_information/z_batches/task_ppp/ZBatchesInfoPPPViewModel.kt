package com.lenta.bp9.features.goods_information.z_batches.task_ppp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.processing.ProcessZBatchesPPPService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
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

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val isPerishable: MutableLiveData<Boolean> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinProductionDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val productionDate: MutableLiveData<String> = MutableLiveData("")
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
                                ?.let { currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?: true
                    }

    val tvAccept: MutableLiveData<String> by lazy {
        val isEizUnit = productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code
        if (isEizUnit) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
            val purchaseOrderUnitsName = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
            val uomName = productInfo.value?.uom?.name.orEmpty()
            val quantity = productInfo.value?.quantityInvest?.toDouble().toStringFormatted()
            MutableLiveData(context.getString(R.string.accept, "$purchaseOrderUnitsName=$quantity $uomName"))
        }
    }

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

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

    private val currentManufacture: String
        get() {
            val position = spinManufacturersSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        spinManufacturers.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentProductionDate: String
        get() {
            val position = spinProductionDateSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        spinProductionDate.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position] }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentProductionDateFormatterEN: String
        get() {
            return currentProductionDate
                    .takeIf { it.isNotEmpty() }
                    ?.run { formatterEN.format(formatterRU.parse(this)) }
                    .orEmpty()
        }



    private val currentTypeDiscrepanciesCode: String
        get() {
            return currentQualityInfoCode
                    .takeIf { it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
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
                                ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS }
                                ?: -1
            } else {
                qualityInfo.value = dataBase.getQualityMercuryInfo().orEmpty()
            }

            /** определяем, что товар скоропорт, это общий для всех алгоритм https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            val productGeneralShelfLife = productInfo.value?.generalShelfLife?.toInt() ?: 0
            val productRemainingShelfLife = productInfo.value?.remainingShelfLife?.toInt() ?: 0
            val productMhdhbDays = productInfo.value?.mhdhbDays ?: 0
            val productMhdrzDays = productInfo.value?.mhdrzDays ?: 0

            isPerishable.value = productGeneralShelfLife > 0
                    || productRemainingShelfLife > 0
                    || (productMhdhbDays in 1 until paramGrzUffMhdhb)

            isPerishable.value
                    ?.takeIf { it }
                    ?.run {
                        if ( productGeneralShelfLife > 0 || productRemainingShelfLife > 0 ) { //https://trello.com/c/XSAxdgjt
                            generalShelfLife.value = productGeneralShelfLife.toString()
                            remainingShelfLife.value = productRemainingShelfLife.toString()
                        } else {
                            generalShelfLife.value = productMhdhbDays.toString()
                            remainingShelfLife.value = productMhdrzDays.toString()
                        }
                    }

            spinQuality.value = qualityInfo.value?.map { it.name }

            spinManufacturers.value =
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.getManufacturesOfProduct(productInfo.value!!)

            /**paramGrzRoundLackRatio.value = dataBase.getParamGrzRoundLackRatio()
            paramGrzRoundLackUnit.value = dataBase.getParamGrzRoundLackUnit()
            paramGrzRoundHeapRatio.value = dataBase.getParamGrzRoundHeapRatio()*/
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

    fun getTitle() : String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
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
                                ?.indexOfLast {it.code == "44"}
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

    fun onClickPositionSpinProductionDate(position: Int) {
        spinProductionDateSelectedPosition.value = position
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
        //searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openGoodsDetailsScreen(it) }
    }

    fun onClickAdd() {
        return
    }

    fun onClickApply() {
        return
    }

}
