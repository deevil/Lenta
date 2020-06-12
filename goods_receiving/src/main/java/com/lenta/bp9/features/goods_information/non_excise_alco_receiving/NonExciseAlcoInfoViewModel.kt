package com.lenta.bp9.features.goods_information.non_excise_alco_receiving

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseAlcoProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class NonExciseAlcoInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processNonExciseAlcoProductService: ProcessNonExciseAlcoProductService
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    private val batchInfo: MutableLiveData<TaskBatchInfo> = MutableLiveData()
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val planQuantityBatch: MutableLiveData<String> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()

    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.combineLatest(isDiscrepancy).map {
        if (!it!!.second) {
            qualityInfo.value?.get(it.first)?.code != "1"
        } else true
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0).plus(
                        batchInfo.value?.let{batch ->
                            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch) ?: 0.0
                        } ?: 0.0
                )
            } else {
                batchInfo.value?.let {batch ->
                    taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch) ?: 0.0
                } ?: 0.0
            }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = batchInfo.value?.let {batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch) ?: 0.0
        } ?: 0.0
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
                    if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                        (it?.first ?: 0.0).plus(
                                batchInfo.value?.let{batch ->
                                    taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch) ?: 0.0
                                } ?: 0.0
                        )
                    } else {
                        batchInfo.value?.let {batch ->
                            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch) ?: 0.0
                        } ?: 0.0
                    }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = batchInfo.value?.let {batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch) ?: 0.0
        } ?: 0.0

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name}"
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.
            map {
                it!! != 0.0
            }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@NonExciseAlcoInfoViewModel::viewModelScope,
                    scanResultHandler = this@NonExciseAlcoInfoViewModel::handleProductSearchResult)

            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)
            suffix.value = productInfo.value?.purchaseOrderUnits?.name

            if (isDiscrepancy.value!!) {
                count.value = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountBatchNotProcessedOfBatch(batchInfo.value!!).toStringFormatted()
                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "4"}
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
                it.code == taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)?.egais ?: ""
            }?.name ?: ""
            spinManufacturers.value = listOf(manufacturerName)
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)?.bottlingDate)) ?: "")
            planQuantityBatch.value = "${batchInfo.value?.purchaseOrderScope.toStringFormatted()} ${productInfo.value!!.purchaseOrderUnits.name}"

            if (processNonExciseAlcoProductService.newProcessNonExciseAlcoProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() : Boolean {
        return if (processNonExciseAlcoProductService.overlimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimit()
            false
        } else {
            if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                processNonExciseAlcoProductService.add(acceptTotalCount.value!!.toString(), "1")
            } else {
                processNonExciseAlcoProductService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
            }
            count.value = "0"
            true
        }
    }

    fun onClickApply() {
        if (onClickAdd()) {
            screenNavigator.goBack()
        }
    }

    fun onScanResult(data: String) {
        if (enabledApplyButton.value == true) {
            if (onClickAdd()) {
                searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
            }
        } else {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinManufacturers(position: Int){
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinottlingDate(position: Int){
        spinBottlingDateSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            }
            if (isDiscrepancy.value!!) {
                spinReasonRejectionSelectedPosition.value = reasonRejectionInfo.value!!.indexOfLast {it.code == "43"}
            } else {
                spinReasonRejectionSelectedPosition.value = 0
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onBackPressed() {
        if (enabledApplyButton.value!!) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
            return
        }

        screenNavigator.goBack()
    }
}
