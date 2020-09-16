package com.lenta.bp9.features.label_printing

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.ZmpUtzGrz45V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz45V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz45V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import java.lang.Exception
import java.text.SimpleDateFormat
import javax.inject.Inject

class LabelPrintingViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var zmpUtzGrz45V001NetRequest: ZmpUtzGrz45V001NetRequest

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val labels: MutableLiveData<List<LabelPrintingItem>> = MutableLiveData()
    val labelSelectionsHelper = SelectionItemsHelper()

    @SuppressLint("SimpleDateFormat")
    val formatterRU = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy)

    @SuppressLint("SimpleDateFormat")
    val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)

    val enabledNextBtn: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            zmpUtzGrz45V001NetRequest(ZmpUtzGrz45V001Params(
                    taskNumber = taskManager
                            .getReceivingTask()
                            ?.taskHeader
                            ?.taskNumber
                            .orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    discrepanciesZBatches = taskManager
                            .getReceivingTask()
                            ?.getProcessedZBatchesDiscrepancies()
                            ?.map { TaskZBatchesDiscrepanciesRestData.from(it) }
                            .orEmpty()
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: ZmpUtzGrz45V001Result) {
        launchUITryCatch {
            val zBatchesDiscrepancies = result.taskZBatchesDiscrepancies
                    ?.map { TaskZBatchesDiscrepancies.from(hyperHive, it) }
                    .orEmpty()

            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getZBatchesDiscrepancies()
                    ?.updateZBatchesDiscrepancy(zBatchesDiscrepancies)

            updateLabels()
        }
    }

    private fun updateLabels() {
        val labelPrintingItems: ArrayList<LabelPrintingItem> = ArrayList()
        val zBatches = taskManager.getReceivingTask()?.getProcessedZBatchesDiscrepancies()

        zBatches?.let {
            it.mapIndexed { index, label ->
                val product = getProductInfoForLabel(label.materialNumber)
                if (product?.isNeedPrint == true) {
                    val productDiscrepancies = getProductDiscrepanciesForLabel(label.materialNumber)
                    val materialLastSix = product.getMaterialLastSix()
                    val partySignsOfZBatches = getPartySignsForLabel(label)
                    val partySign = partySignsOfZBatches?.partySign?.partySignsTypeString.orEmpty()
                    val manufacturerName = getManufacturerName(label.manufactureCode)
                    val numberDiscrepancies = productDiscrepancies?.numberDiscrepancies.orEmpty()
                    val unitName = product.uom.name
                    val shelfLifeOrProductionDate = getShelfLifeOrProductionDate(label)

                    labelPrintingItems.add(
                            LabelPrintingItem(
                                    number = index + 1,
                                    productName = "$materialLastSix ${product.description}",
                                    batchName = "${partySign}-$shelfLifeOrProductionDate // $manufacturerName",
                                    quantityUnit = "$numberDiscrepancies $unitName",
                                    isPrinted = false,
                                    shelfLife = "${getDateFormatterRU(partySignsOfZBatches?.shelfLifeDate.orEmpty())} ${partySignsOfZBatches?.shelfLifeTime.orEmpty()}",
                                    productionDate = "${getDateFormatterRU(partySignsOfZBatches?.productionDate.orEmpty())} ${partySignsOfZBatches?.shelfLifeTime.orEmpty()}",
                                    batchDiscrepancies = label
                            )
                    )
                }
            }
        }

        labels.value = labelPrintingItems.reversed()
        labelSelectionsHelper.clearPositions()
    }

    private fun getProductDiscrepanciesForLabel(materialNumber: String): TaskProductDiscrepancies? {
        return taskManager
                .getReceivingTask()
                ?.getProcessedProductsDiscrepancies()
                ?.findLast { productDiscr -> productDiscr.materialNumber == materialNumber }
    }

    private fun getProductInfoForLabel(materialNumber: String): TaskProductInfo? {
        return taskManager
                .getReceivingTask()
                ?.getProcessedProducts()
                ?.findLast { product -> product.materialNumber == materialNumber }
    }

    private fun getPartySignsForLabel(label: TaskZBatchesDiscrepancies): PartySignsOfZBatches? {
        return taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getZBatchesDiscrepancies()
                ?.findPartySignOfZBatch(label)
    }

    private fun getShelfLifeOrProductionDate(zBatchesDiscrepancies: TaskZBatchesDiscrepancies): String {
        return try {
            val partySignOfZBatch =
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getZBatchesDiscrepancies()
                            ?.findPartySignOfZBatch(zBatchesDiscrepancies)

            when(partySignOfZBatch?.partySign ?: PartySignsTypeOfZBatches.None) {
                PartySignsTypeOfZBatches.ProductionDate -> {
                    partySignOfZBatch
                            ?.productionDate
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { formatterRU.format(formatterERP.parse(it)) }
                            .orEmpty()
                }
                PartySignsTypeOfZBatches.ShelfLife -> {
                    partySignOfZBatch
                            ?.shelfLifeDate
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { formatterRU.format(formatterERP.parse(it)) }
                            .orEmpty()
                }
                else -> ""
            }
        }
        catch (e: Exception) {
            Logg.e { "e: $e" }
            ""
        }
    }

    private fun getManufacturerName(manufacturerCode: String): String {
        return repoInMemoryHolder
                .manufacturersForZBatches.value
                ?.findLast { it.manufactureCode == manufacturerCode }
                ?.manufactureName
                .orEmpty()
    }

    private fun getDateFormatterRU(dateFormatterERP: String): String {
        return try {
            formatterRU.format(formatterERP.parse(dateFormatterERP))
        } catch (e: Exception){
            Logg.e { "e: $e" }
            ""
        }
    }

    fun getTitle(): String {
        return taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.caption
                .orEmpty()
    }

    fun markPrintedLabels(bundle: Bundle) {
        bundle.getStringArrayList("printedLabels")
                ?.map {
                    it?.let { batchNumber ->
                        labels.value
                                ?.findLast { item -> item.batchDiscrepancies?.batchNumber == batchNumber }
                                ?.isPrinted = true
                    }
                }

        buttonStateNext()
    }

    private fun buttonStateNext() {
        val totalCountLabels = labels.value?.size ?: 0
        val countPrintedLabels = labels.value?.filter { it.isPrinted }?.size ?: 0
        enabledNextBtn.value = countPrintedLabels == totalCountLabels
    }

    fun onClickPrint() {
        val selectedLabels: ArrayList<LabelPrintingItem> = ArrayList()
        val labelSelectionsHelperSize = labelSelectionsHelper.selectedPositions.value?.size ?: 0

        if (labelSelectionsHelperSize <= 0) {
            labels.value?.mapTo(selectedLabels) { it.copy() }
        } else {
            labelSelectionsHelper
                    .selectedPositions.value
                    ?.map { position -> labels.value?.get(position) }
                    ?.mapNotNullTo(selectedLabels) { it?.copy() }
        }

        screenNavigator.openPrintLabelsCountCopiesScreen(selectedLabels)
        labelSelectionsHelper.clearPositions()
    }

    fun onClickNext() {
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType
                ?: TaskType.None)
    }

}

data class LabelPrintingItem(
        val number: Int,
        val productName: String,
        val batchName: String,
        val quantityUnit: String,
        var isPrinted: Boolean,
        val shelfLife: String,
        val productionDate: String,
        val batchDiscrepancies: TaskZBatchesDiscrepancies?
)
