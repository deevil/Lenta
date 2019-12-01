package com.lenta.bp9.features.discrepancy_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskDescription
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel(), PageSelectionListener {

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

    val selectedPage = MutableLiveData(0)
    val processedSelectionsHelper = SelectionItemsHelper()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = processedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val enabledSaveButton: MutableLiveData<Boolean> = countProcessed.map {
        taskManager.getReceivingTask()!!.taskRepository.getProducts().getProducts().size <= (it?.size ?: 0)
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> = MutableLiveData()

    fun onResume() {
        visibilityBatchesButton.value = taskManager.getReceivingTask()?.taskDescription?.isAlco
        updateCountNotProcessed()
        updateCountProcessed()
    }

    private fun updateCountNotProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                countNotProcessed.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(it) > 0.0
                                }
                                .mapIndexed { index, productInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAcceptWithUom = "",
                                            countRefusalWithUom = "",
                                            quantityNotProcessedWithUom = "? ${task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(productInfo).toStringFormatted()} ${productInfo.uom.name}",
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                countNotProcessed.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    task.taskRepository.getBatchesDiscrepancies().getCountBatchNotProcessedOfBatch(it) > 0.0
                                }
                                .mapIndexed { index, batchInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAcceptWithUom = "",
                                            countRefusalWithUom = "",
                                            quantityNotProcessedWithUom = "? ${task.taskRepository.getBatchesDiscrepancies().getCountBatchNotProcessedOfBatch(batchInfo).toStringFormatted()} ${batchInfo.uom.name}",
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }

        }
    }

    private fun updateCountProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                countProcessed.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                            task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it) > 0.0
                                }.mapIndexed { index, productInfo ->
                                    val acceptTotalCount = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo)
                                    val acceptTotalCountWithUom = if (acceptTotalCount != 0.0) {
                                        "+ " + acceptTotalCount.toStringFormatted() + " " + productInfo.uom.name
                                    } else {
                                        "0 " + productInfo.uom.name
                                    }
                                    val refusalTotalCount = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo)
                                    val refusalTotalCountWithUom = if (refusalTotalCount != 0.0) {
                                        "- " + refusalTotalCount.toStringFormatted() + " " + productInfo.uom.name
                                    } else {
                                        "0 " + productInfo.uom.name
                                    }
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAcceptWithUom = acceptTotalCountWithUom,
                                            countRefusalWithUom = refusalTotalCountWithUom,
                                            quantityNotProcessedWithUom = "",
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                countProcessed.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) +
                                            task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(it) > 0.0
                                }.mapIndexed { index, batchInfo ->
                                    val acceptTotalCount = task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(batchInfo)
                                    val acceptTotalCountWithUom = if (acceptTotalCount != 0.0) {
                                        "+ " + acceptTotalCount.toStringFormatted() + " " + batchInfo.uom.name
                                    } else {
                                        "0 " + batchInfo.uom.name
                                    }
                                    val refusalTotalCount = task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(batchInfo)
                                    val refusalTotalCountWithUom = if (refusalTotalCount != 0.0) {
                                        "- " + refusalTotalCount.toStringFormatted() + " " + batchInfo.uom.name
                                    } else {
                                        "0 " + batchInfo.uom.name
                                    }
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAcceptWithUom = acceptTotalCountWithUom,
                                            countRefusalWithUom = refusalTotalCountWithUom,
                                            quantityNotProcessedWithUom = "",
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
        }
        processedSelectionsHelper.clearPositions()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val matnr: String?
        if (selectedPage.value == 0) {
            matnr = countNotProcessed.value?.get(position)?.productInfo?.materialNumber
        } else {
            matnr = countProcessed.value?.get(position)?.productInfo?.materialNumber
        }
        matnr?.let {
            val productInfo = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.findProduct(it)
            if (productInfo != null) {
                if (productInfo.isVet) {
                    screenNavigator.openGoodsMercuryInfoScreen(productInfo, true)
                } else {
                    when (productInfo.type) {
                        ProductType.General -> screenNavigator.openGoodsInfoScreen(productInfo, true)
                        ProductType.ExciseAlcohol -> screenNavigator.openExciseAlcoInfoScreen(productInfo)
                        ProductType.NonExciseAlcohol -> screenNavigator.openNonExciseAlcoInfoScreen(productInfo)
                    }
                }
            }
        }
    }

    fun onClickClean() {
        if (!isBatches.value!!) {
            processedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.deleteProductsDiscrepanciesForProduct(countProcessed.value?.get(position)!!.productInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(countProcessed.value?.get(position)!!.productInfo!!.copy(isNoEAN = true))
            }
        } else {
            processedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.deleteBatchesDiscrepanciesForBatch(countProcessed.value?.get(position)!!.batchInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getBatches()?.changeBatch(countProcessed.value?.get(position)!!.batchInfo!!.copy(isNoEAN = true))
            }
        }

        updateCountNotProcessed()
        updateCountProcessed()
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateCountNotProcessed()
        updateCountProcessed()
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            endRecountDirectDeliveries(EndRecountDDParameters(
                    taskNumber = taskManager.getReceivingTask()!!.taskHeader.taskNumber,
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies(),
                    discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies()
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full)
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

}
