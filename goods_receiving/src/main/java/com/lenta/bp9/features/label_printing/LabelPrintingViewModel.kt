package com.lenta.bp9.features.label_printing

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.revise.BatchVM
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class LabelPrintingViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val labels: MutableLiveData<List<LabelPrintingItem>> = MutableLiveData()
    val labelSelectionsHelper = SelectionItemsHelper()

    val enabledNextBtn: MutableLiveData<Boolean> = labelSelectionsHelper.selectedPositions.map {
        !labelSelectionsHelper.selectedPositions.value.isNullOrEmpty()
    }

    fun getTitle(): String {
        return taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.caption
                .orEmpty()
    }

    private fun updateLabels() {
        val zBatches = taskManager.getReceivingTask()?.getProcessedZBatchesDiscrepancies()

        zBatches?.let {
            labels.value =
                    it.mapIndexed { index, label ->
                        val productDiscrepancies = taskManager.getReceivingTask()?.getProcessedProductsDiscrepancies()?.findLast { productDiscr-> productDiscr.materialNumber == label.materialNumber }
                        val product = taskManager.getReceivingTask()?.getProcessedProducts()?.findLast { product-> product.materialNumber == label.materialNumber }
                        LabelPrintingItem(
                                number = index + 1,
                                productName = "${product?.getMaterialLastSix().orEmpty()} ${product?.description.orEmpty()}",
                                batchName = "ДП-${label.shelfLifeDate} // ${label.manufactureCode}", //todo заменить на наименование производителя
                                quantityUnit = "${productDiscrepancies?.numberDiscrepancies.orEmpty()} ${product?.uom?.name.orEmpty()}",
                                isPrinted = batch.isCheck,
                                batchNumber = label.batchNumber
                        )
                    }
                    .reversed()
        }
    }

    fun onResume() {
        updateLabels()
    }

}

data class LabelPrintingItem(
        val number: Int,
        val productName: String,
        val batchName: String,
        val quantityUnit: String,
        val isPrinted: Boolean,
        val batchNumber: String
)
