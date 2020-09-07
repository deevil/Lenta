package com.lenta.bp9.features.label_printing

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LabelPrintingViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val labels: MutableLiveData<List<LabelPrintingItem>> = MutableLiveData()
    val labelSelectionsHelper = SelectionItemsHelper()

    val enabledNextBtn: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        launchUITryCatch {
            updateLabels()
        }
    }

    private fun updateLabels() {
        val zBatches = taskManager.getReceivingTask()?.getProcessedZBatchesDiscrepancies()

        zBatches?.let {
            labels.value =
                    listOf(LabelPrintingItem(
                            number = 3,
                            productName = "000021 Соль",
                            batchName = "ДП-04.09.2020 123456789",
                            quantityUnit = "30 шт",
                            isPrinted = false,
                            productionDate = "ДП-04.09.2020",
                            batchNumber = "3"
                    ),
                            LabelPrintingItem(
                                    number = 2,
                                    productName = "000055 Сахар",
                                    batchName = "ДП-08.08.2020 987654321",
                                    quantityUnit = "5 шт",
                                    isPrinted = false,
                                    productionDate = "ДП-08.08.2020",
                                    batchNumber = "2"
                            ),
                            LabelPrintingItem(
                                    number = 1,
                                    productName = "000077 Мука",
                                    batchName = "ДП-12.12.2020 456",
                                    quantityUnit = "77 шт",
                                    isPrinted = false,
                                    productionDate = "ДП-12.12.2020",
                                    batchNumber = "1"
                            ))
                    /**it.mapIndexed { index, label ->
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
                                batchName = "ДП-${label.shelfLifeDate} // ${label.manufactureCode}", //todo заменить на наименование производителя
                                quantityUnit = "${productDiscrepancies?.numberDiscrepancies.orEmpty()} ${product?.uom?.name.orEmpty()}",
                                isPrinted = false,
                                productionDate = "",
                                batchNumber = label.batchNumber
                        )
                    }.reversed()*/
        }

        labelSelectionsHelper.clearPositions()
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
                                ?.findLast { item -> item.batchNumber == batchNumber }
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

    private fun filterClearTabTaskDiff(productInfo: TaskProductInfo) : Boolean {
        //партионный - это помеченный IS_ALCO и не помеченный IS_BOX_FL, IS_MARK_FL (Артем)
        val isBatchNonExciseAlcoholProduct =
                productInfo.type == ProductType.NonExciseAlcohol
                        && !productInfo.isBoxFl
                        && !productInfo.isMarkFl
        val isVetProduct = productInfo.isVet && !productInfo.isNotEdit
        //коробочный или марочный алкоголь
        val isExciseAlcoholProduct =
                productInfo.type == ProductType.ExciseAlcohol
                        && (productInfo.isBoxFl || productInfo.isMarkFl)
        return isBatchNonExciseAlcoholProduct
                || isVetProduct
                || isExciseAlcoholProduct
    }

    fun onClickNext() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            /**
             * очищаем таблицу ET_TASK_DIFF от не акцизного (партионного) алкоголя и веттоваров,
             * т.к. для партионного товара необходимо передавать только данные из таблицы ET_PARTS_DIFF, а для веттоваров - ET_VET_DIFF
             */
            val receivingTask = taskManager.getReceivingTask()
            val taskRepository = taskManager.getReceivingTask()?.taskRepository
            receivingTask
                    ?.getProcessedProductsDiscrepancies()
                    ?.mapNotNull { productDiscr ->
                        taskRepository
                                ?.getProducts()
                                ?.findProduct(productDiscr.materialNumber)
                    }
                    ?.filter { filterClearTabTaskDiff(it) }
                    ?.forEach { productForDel ->
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.deleteProductsDiscrepanciesForProduct(productForDel.materialNumber)
                    }

            endRecountDirectDeliveries(EndRecountDDParameters(
                    taskNumber = receivingTask
                            ?.taskHeader
                            ?.taskNumber
                            .orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    discrepanciesProduct = receivingTask
                            ?.getProcessedProductsDiscrepancies()
                            ?.map { TaskProductDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    discrepanciesBatches = receivingTask
                            ?.getProcessedBatchesDiscrepancies()
                            ?.map { TaskBatchesDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    discrepanciesBoxes = receivingTask
                            ?.getProcessedBoxesDiscrepancies()
                            ?.map { TaskBoxDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    discrepanciesExciseStamp = receivingTask
                            ?.getProcessedExciseStampsDiscrepancies()
                            ?.map { TaskExciseStampDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    exciseStampBad = receivingTask
                            ?.getProcessedExciseStampsBad()
                            ?.map { TaskExciseStampBadRestData.from(it) }
                            .orEmpty(),
                    discrepanciesMercury = receivingTask
                            ?.getProcessedMercuryDiscrepancies()
                            ?.map { TaskMercuryDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    discrepanciesBlocks = receivingTask
                            ?.getProcessedBlocksDiscrepancies()
                            ?.map { TaskBlockDiscrepanciesRestData.from(it) }
                            .orEmpty(),
                    discrepanciesZBatches = receivingTask
                            ?.getProcessedZBatchesDiscrepancies()
                            ?.map { TaskZBatchesDiscrepanciesRestData.from(it) }
                            .orEmpty()
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
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
        val productionDate: String,
        val batchNumber: String
)
