package com.lenta.bp9.features.goods_information.non_excise_alco_pge

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseAlcoProductPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class NonExciseAlcoInfoPGEViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processNonExciseAlcoProductPGEService: ProcessNonExciseAlcoProductPGEService

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
    val planQuantityBatch: MutableLiveData<String> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        if (qualityInfo.value?.get(it!!.second)?.code == "1") {
            (it?.first ?: 0.0).plus(
                    batchInfo.value?.let { batch ->
                        taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
                                ?: 0.0
                    } ?: 0.0
            )
        } else {
            batchInfo.value?.let { batch ->
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
                        ?: 0.0
            } ?: 0.0
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = batchInfo.value?.let { batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
                    ?: 0.0
        } ?: 0.0
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
            (it?.first ?: 0.0).plus(
                    batchInfo.value?.let { batch ->
                        taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
                                ?: 0.0
                    } ?: 0.0
            )
        } else {
            batchInfo.value?.let { batch ->
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
                        ?: 0.0
            } ?: 0.0
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = batchInfo.value?.let { batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
                    ?: 0.0
        } ?: 0.0

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@NonExciseAlcoInfoPGEViewModel::viewModelScope,
                    scanResultHandler = this@NonExciseAlcoInfoPGEViewModel::handleProductSearchResult)

            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)
            suffix.value = productInfo.value?.uom?.name

            if (isDiscrepancy.value!!) {
                count.value = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountBatchNotProcessedOfBatchPGE(batchInfo.value!!).toStringFormatted()
                qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast { it.code == "4" }
            } else {
                qualityInfo.value = dataBase.getQualityInfoPGENotSurplus()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
                it.code == taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)?.egais ?: ""
            }?.name ?: ""
            spinManufacturers.value = listOf(manufacturerName)
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)?.bottlingDate))
                    ?: "")
            planQuantityBatch.value = "${batchInfo.value?.purchaseOrderScope.toStringFormatted()} ${productInfo.value!!.uom.name}"

            spinProcessingUnit.value = listOf("ЕО - " + batchInfo.value?.processingUnitNumber)

            if (processNonExciseAlcoProductPGEService.newProcessNonExciseAlcoProductPGEService(productInfo.value!!) == null) {
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

    fun onClickAdd(): Boolean {
        return if (processNonExciseAlcoProductPGEService.overlimit(countValue.value!!)) {
            screenNavigator.openExceededPlannedQuantityBatchPGEDialog(
                    nextCallbackFunc = {
                        processNonExciseAlcoProductPGEService.addSurplus(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinProcessingUnit.value!![spinProcessingUnitSelectedPosition.value!!].substring(5))
                        count.value = "0"
                    }
            )
            false
        } else {
            processNonExciseAlcoProductPGEService.add(count.value!!, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinProcessingUnit.value!![spinProcessingUnitSelectedPosition.value!!].substring(5))
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
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
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
