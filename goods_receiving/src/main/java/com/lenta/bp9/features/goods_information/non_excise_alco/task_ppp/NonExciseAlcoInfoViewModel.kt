package com.lenta.bp9.features.goods_information.non_excise_alco.task_ppp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseAlcoProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
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
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)

    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.combineLatest(isDiscrepancy).map {
        if (!it!!.second) {
            qualityInfo.value?.get(it.first)?.code != "1"
        } else true
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val batchInfo: MutableLiveData<List<TaskBatchInfo>> = MutableLiveData()
    private val manufacturer: MutableLiveData<List<Manufacturer>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0).plus(
                        batchInfo.value?.map {batch ->
                            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch)
                        }?.sumByDouble {count ->
                            count ?: 0.0
                        } ?: 0.0
                )
            } else {
                batchInfo.value?.map {batch ->
                    taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch)
                }?.sumByDouble {count ->
                    count ?: 0.0
                } ?: 0.0
            }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = batchInfo.value?.map {batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batch)
        }?.sumByDouble {count ->
            count ?: 0.0
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
                                batchInfo.value?.map {batch ->
                                    taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch)
                                }?.sumByDouble {count ->
                                    count ?: 0.0
                                } ?: 0.0
                        )
                    } else {
                        batchInfo.value?.map {batch ->
                            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch)
                        }?.sumByDouble {count ->
                            count ?: 0.0
                        } ?: 0.0
                    }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = batchInfo.value?.map {batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batch)
        }?.sumByDouble {count ->
            count ?: 0.0
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
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processNonExciseAlcoProductService.newProcessNonExciseAlcoProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@NonExciseAlcoInfoViewModel::handleProductSearchResult)

            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)
            suffix.value = productInfo.value?.purchaseOrderUnits?.name

            if (isDiscrepancy.value!!) {
                batchInfo.value?.let {
                    count.value = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountBatchNotProcessedOfBatch(it[0]).toStringFormatted()
                }

                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                qualityInfo.value?.let {quality ->
                    spinQualitySelectedPosition.value = quality.indexOfLast {it.code == "4"}
                }
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            manufacturer.value = batchInfo.value?.mapNotNull { batch ->
                repoInMemoryHolder.manufacturers.value?.findLast {
                    it.code == batch.egais
                }
            }
            spinManufacturers.value = manufacturer.value?.groupBy {
                it.name
            }?.map {
                it.key
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
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(spinManufacturersSelectedPosition.value ?: 0)
        }?.code
        val batchSelected = batchInfo.value?.findLast {batch ->
            batch.egais == manufactureCode && batch.bottlingDate == formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value ?: 0)))
        }

        return if (batchSelected != null) {
            if (processNonExciseAlcoProductService.overLimit(countValue.value!!, batchSelected)) {
                screenNavigator.openAlertOverLimit()
                false
            } else {
                if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                    processNonExciseAlcoProductService.add(count.value!!, "1", batchSelected)
                } else {
                    processNonExciseAlcoProductService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, batchSelected)
                }
                count.value = "0"
                true
            }
        } else false
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

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
        updateDataSpinBottlingDate(position)
    }

    fun onClickPositionBottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
        batchInfo.value?.let {
            planQuantityBatch.value = "${it[position].purchaseOrderScope.toStringFormatted()} ${productInfo.value!!.purchaseOrderUnits.name}"
        }
    }

    private fun updateDataSpinBottlingDate(position: Int) {
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(position)
        }?.code

        val bottlingDates = batchInfo.value?.filter {
            it.egais == manufactureCode
        }?.map {batch ->
            formatterRU.format(formatterEN.parse(batch.bottlingDate))
        }
        spinBottlingDateSelectedPosition.value = 0
        spinBottlingDate.value = bottlingDates

        batchInfo.value?.findLast {batch ->
            batch.egais == manufactureCode && batch.bottlingDate == formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value!!)))
        }?.let {
            planQuantityBatch.value = "${it.purchaseOrderScope.toStringFormatted()} ${productInfo.value!!.purchaseOrderUnits.name}"
        }
    }

    fun onClickPositionSpinQuality(position: Int){
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
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
