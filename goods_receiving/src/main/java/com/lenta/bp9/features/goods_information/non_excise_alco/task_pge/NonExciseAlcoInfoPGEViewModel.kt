package com.lenta.bp9.features.goods_information.non_excise_alco.task_pge

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessNonExciseAlcoProductPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import java.text.SimpleDateFormat
import javax.inject.Inject

private const val PROCESSING_UNIT_NUMBER_LENGTH = 18

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
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val batchInfo: MutableLiveData<List<TaskBatchInfo>> = MutableLiveData()
    private val manufacturer: MutableLiveData<List<Manufacturer>> = MutableLiveData()
    private val indexSpinProcessingUnit: MutableLiveData<Int> = MutableLiveData(0)
    private val isSelectedHeadingSpinProcessingUnit: MutableLiveData<Boolean> = MutableLiveData(false)
    val enteredProcessingUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy { //https://trello.com/c/WQg659Ww
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true)
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).combineLatest(spinManufacturersSelectedPosition).map {
        if (qualityInfo.value?.get(it!!.first.second)?.code == "1") {
            (it?.first?.first ?: 0.0).plus(
                    batchInfo.value?.map { batch ->
                        taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
                    }?.sumByDouble { count ->
                        count ?: 0.0
                    } ?: 0.0
            )
        } else {
            batchInfo.value?.map { batch ->
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
            }?.sumByDouble { count ->
                count ?: 0.0
            } ?: 0.0
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = batchInfo.value?.map { batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batch)
        }?.sumByDouble { count ->
            count ?: 0.0
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

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).combineLatest(spinManufacturersSelectedPosition).map {
        if (qualityInfo.value?.get(it?.first?.second ?: 0)?.code != "1") {
            (it?.first?.first ?: 0.0).plus(
                    batchInfo.value?.map { batch ->
                        taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
                    }?.sumByDouble { count ->
                        count ?: 0.0
                    } ?: 0.0
            )
        } else {
            batchInfo.value?.map { batch ->
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
            }?.sumByDouble { count ->
                count ?: 0.0
            } ?: 0.0
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = batchInfo.value?.map { batch ->
            taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batch)
        }?.sumByDouble { count ->
            count ?: 0.0
        } ?: 0.0

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue
            .combineLatest(bottlingDate)
            .combineLatest(enteredProcessingUnitNumber)
            .combineLatest(isSelectedHeadingSpinProcessingUnit)
            .map {
                val totalCount = countValue.value ?: 0.0
                val bottlingDateValueNotEmpty = bottlingDate.value?.isNotEmpty() ?: false
                val processingUnitNumberLength = enteredProcessingUnitNumber.value?.length
                if (isGoodsAddedAsSurplus.value == true) {
                    totalCount > 0.0 && bottlingDateValueNotEmpty && processingUnitNumberLength == PROCESSING_UNIT_NUMBER_LENGTH
                } else {
                    totalCount > 0.0 && isSelectedHeadingSpinProcessingUnit.value == false
                }
            }

    val enabledAddBtn: MutableLiveData<Boolean> = enabledApplyButton.map {
        if (isGoodsAddedAsSurplus.value == true) {
            false
        } else {
            it
        }
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processNonExciseAlcoProductPGEService.newProcessNonExciseAlcoProductPGEService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@NonExciseAlcoInfoPGEViewModel::handleProductSearchResult)

            batchInfo.value = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo.value!!)
            suffix.value = productInfo.value?.uom?.name

            when {
                isGoodsAddedAsSurplus.value == true -> {
                    suffix.value = productInfo.value?.uom?.name
                    qualityInfo.value = dataBase.getSurplusInfoForPGE()
                }
                isDiscrepancy.value == true -> {
                    batchInfo.value?.let {
                        count.value = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountBatchNotProcessedOfBatch(it[0]).toStringFormatted()
                    }

                    qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                    qualityInfo.value?.let { quality ->
                        spinQualitySelectedPosition.value = quality.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD }
                    }
                }
                else -> qualityInfo.value = dataBase.getQualityInfoPGENotSurplus()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            if (isGoodsAddedAsSurplus.value == true) {
                manufacturer.value = repoInMemoryHolder.manufacturers.value?.map {
                    it
                }
            } else {
                manufacturer.value = batchInfo.value?.mapNotNull { batch ->
                    repoInMemoryHolder.manufacturers.value?.findLast {
                        it.code == batch.egais
                    }
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

    fun onClickAdd(): Boolean {
        val totalCount = count.value.orEmpty()
        val prefixProcessingUnitLength = context.getString(R.string.prefix_processing_unit).length
        val spinProcessingUnitSelectedPositionValue = spinProcessingUnitSelectedPosition.value ?: 0
        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPosition.value
                ?: 0)?.code.orEmpty()
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(spinManufacturersSelectedPosition.value ?: 0)
        }?.code
        val selectedBottlingDate = spinBottlingDate.value?.get(spinBottlingDateSelectedPosition.value
                ?: 0).orEmpty()
        val selectedProcessingUnitNumber = spinProcessingUnit.value?.get(spinProcessingUnitSelectedPositionValue)?.substring(prefixProcessingUnitLength).orEmpty()
        val batchSelected = batchInfo
                .value
                ?.findLast { batch ->
                    batch.egais == manufactureCode
                            && batch.bottlingDate == formatterEN.format(formatterRU.parse(selectedBottlingDate))
                            && batch.processingUnitNumber == selectedProcessingUnitNumber
                }

        return if (isGoodsAddedAsSurplus.value == true && totalCount.isNotEmpty() && qualityInfoCode.isNotEmpty()) {
            processNonExciseAlcoProductPGEService.addGoodsAddedAsSurplus(
                    count = totalCount,
                    typeDiscrepancies = qualityInfoCode,
                    processingUnit = enteredProcessingUnitNumber.value.orEmpty()
            )
            count.value = "0"
            true
        } else {
            if (batchSelected != null
                    && totalCount.isNotEmpty()
                    && qualityInfoCode.isNotEmpty()
                    && selectedProcessingUnitNumber.isNotEmpty()
            ) {
                if (processNonExciseAlcoProductPGEService.overLimit(totalCount.toDouble(), batchSelected)) {
                    screenNavigator.openExceededPlannedQuantityBatchPGEDialog(
                            nextCallbackFunc = {
                                processNonExciseAlcoProductPGEService.addSurplus(
                                        count = totalCount,
                                        typeDiscrepancies = qualityInfoCode,
                                        processingUnit = selectedProcessingUnitNumber,
                                        batchInfo = batchSelected
                                )
                                count.value = "0"
                            }
                    )
                    false
                } else {
                    processNonExciseAlcoProductPGEService.add(
                            count = totalCount,
                            typeDiscrepancies = qualityInfoCode,
                            processingUnit = selectedProcessingUnitNumber,
                            batchInfo = batchSelected
                    )
                    count.value = "0"
                    true
                }
            } else false
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
        setPlanQuantityBatch(position)
    }

    fun onClickPositionSpinManufacturers(position: Int) {
        spinManufacturersSelectedPosition.value = position
        updateDataSpinBottlingDate(position)
    }

    fun onClickPositionBottlingDate(position: Int) {
        spinBottlingDateSelectedPosition.value = position
        if (isGoodsAddedAsSurplus.value == false) updateDataSpinProcessingUnit(position)
    }

    fun onClickPositionSpinQuality(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    private fun updateDataSpinBottlingDate(position: Int) {
        val manufactureCode =
                manufacturer.value
                        ?.findLast {
                            it.name == spinManufacturers.value?.get(position)
                        }
                        ?.code

        val bottlingDates =
                batchInfo.value
                        ?.filter { batch ->
                            batch.egais == manufactureCode
                        }
                        ?.groupBy { dateGroups ->
                            formatterRU.format(formatterEN.parse(dateGroups.bottlingDate))
                        }
                        ?.map {
                            it.key
                        }
        spinBottlingDateSelectedPosition.value = 0
        spinBottlingDate.value = bottlingDates

        if (isGoodsAddedAsSurplus.value == false) updateDataSpinProcessingUnit(spinBottlingDateSelectedPosition.value
                ?: 0)
    }

    private fun updateDataSpinProcessingUnit(positionSpinBottlingDate: Int) {
        val manufactureCode = manufacturer.value?.findLast {
            it.name == spinManufacturers.value?.get(spinManufacturersSelectedPosition.value ?: 0)
        }?.code

        val bottlingDate = formatterEN.format(formatterRU.parse(spinBottlingDate.value?.get(positionSpinBottlingDate)))

        var listProcessingUnitNumber =
                batchInfo.value
                        ?.filter { batch ->
                            batch.egais == manufactureCode && batch.bottlingDate == bottlingDate
                        }
                        ?.groupBy { processingUnitNumberGroups ->
                            processingUnitNumberGroups.processingUnitNumber
                        }
                        ?.map {
                            "${context.getString(R.string.prefix_processing_unit)}${it.key}"
                        }
                        .orEmpty()

        if (listProcessingUnitNumber.size > 1) {
            listProcessingUnitNumber = listOf(context.getString(R.string.selected_processing_unit)).plus(listProcessingUnitNumber)
            indexSpinProcessingUnit.value = 1
        }
        spinProcessingUnit.value = listProcessingUnitNumber
        spinProcessingUnitSelectedPosition.value = 0
        setPlanQuantityBatch(0)
    }

    private fun setPlanQuantityBatch(position: Int) {
        val indexSpinValue = indexSpinProcessingUnit.value ?: 0
        isSelectedHeadingSpinProcessingUnit.value = indexSpinValue > 0 && position == 0
        batchInfo.value?.let { batch ->
            planQuantityBatch.value = isSelectedHeadingSpinProcessingUnit.value
                    ?.takeIf { !it }
                    ?.run {
                        val actualPosition = position - indexSpinValue
                        "${batch.getOrNull(actualPosition)?.purchaseOrderScope.toStringFormatted()} ${productInfo.value?.uom?.name.orEmpty()}"
                    }.orEmpty()
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
