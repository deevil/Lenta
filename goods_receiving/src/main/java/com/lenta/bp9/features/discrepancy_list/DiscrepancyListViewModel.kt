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
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
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
                                    (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                            task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it)) == 0.0
                                }
                                .mapIndexed { index, productInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAccept = 0.0,
                                            countRefusal = 0.0,
                                            uomName = productInfo.uom.name,
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                countNotProcessed.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    (task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) +
                                            task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it)) == 0.0
                                }
                                .mapIndexed { index, batchInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAccept = 0.0,
                                            countRefusal = 0.0,
                                            uomName = batchInfo.uom.name,
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
                                    task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) > 0.0
                                            || task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it) > 0.0
                                }.mapIndexed { index, productInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAccept = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo),
                                            countRefusal = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo),uomName = productInfo.uom.name,
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                countProcessed.postValue(
                        task.getProcessedBatches()
                                .filter {
                                    task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) > 0.0
                                            || task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(it) > 0.0
                                }.mapIndexed { index, batchInfo ->
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description} \nДР-${batchInfo.bottlingDate} // ${batchInfo.manufacturer}",
                                            countAccept = task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(batchInfo),
                                            countRefusal = task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(batchInfo),
                                            uomName = batchInfo.uom.name,
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
            if (productInfo != null) screenNavigator.openGoodsInfoScreen(productInfo)
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

}
