package com.lenta.bp9.features.label_printing

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.gson.annotations.SerializedName
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
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
        val zBatches = taskManager.getReceivingTask()?.getProcessedZBatchesDiscrepancies()

        zBatches?.let {
            labels.value =
                    it.mapIndexed { index, label ->
                        val productDiscrepancies =
                                taskManager
                                        .getReceivingTask()
                                        ?.getProcessedProductsDiscrepancies()
                                        ?.findLast { productDiscr -> productDiscr.materialNumber == label.materialNumber }

                        val product =
                                taskManager
                                        .getReceivingTask()
                                        ?.getProcessedProducts()
                                        ?.findLast { product -> product.materialNumber == label.materialNumber }

                        LabelPrintingItem(
                                number = index + 1,
                                productName = "${product?.getMaterialLastSix().orEmpty()} ${product?.description.orEmpty()}",
                                batchName = "ДП-${label.shelfLifeDate} // ${getManufacturerName(label.manufactureCode)}",
                                quantityUnit = "${productDiscrepancies?.numberDiscrepancies.orEmpty()} ${product?.uom?.name.orEmpty()}",
                                isPrinted = false,
                                productionDate = "",
                                batchDiscrepancies = label
                        )
                    }.reversed()
        }

        labelSelectionsHelper.clearPositions()
    }

    private fun getManufacturerName(manufacturerCode: String) : String {
        return repoInMemoryHolder
                .manufacturersForZBatches.value
                ?.findLast { it.manufactureCode == manufacturerCode }
                ?.manufactureName
                .orEmpty()
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
            labels.value
                    ?.mapTo(selectedLabels) {it.copy()}
        } else {
            labelSelectionsHelper
                    .selectedPositions.value
                    ?.map { position -> labels.value?.get(position) }
                    ?.mapNotNullTo(selectedLabels) {it?.copy()}
        }

        screenNavigator.openPrintLabelsCountCopiesScreen(selectedLabels)
        labelSelectionsHelper.clearPositions()
    }

    fun onClickNext() {
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

}

data class LabelPrintingItem(
        val number: Int,
        val productName: String,
        val batchName: String,
        val quantityUnit: String,
        var isPrinted: Boolean,
        val productionDate: String,
        val batchDiscrepancies: TaskZBatchesDiscrepancies?
)
