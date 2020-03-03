package com.lenta.bp9.features.discrepancy_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
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
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val selectedPage = MutableLiveData(0)
    val processedSelectionsHelper = SelectionItemsHelper()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

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

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@DiscrepancyListViewModel::viewModelScope,
                    scanResultHandler = this@DiscrepancyListViewModel::handleProductSearchResult)
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        return false
    }

    fun onResume() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                qualityInfo.value = dataBase.getQualityInfoPGE()
            } else {
                qualityInfo.value = dataBase.getAllReasonRejectionInfo()?.map {
                    QualityInfo(
                            id = it.id,
                            code = it.code,
                            name = it.name
                    )
                }
            }
            visibilityBatchesButton.value = taskManager.getReceivingTask()?.taskDescription?.isAlco
            updateCountNotProcessed()
            updateCountProcessed()
            screenNavigator.hideProgress()
        }
    }

    private fun updateCountNotProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                countNotProcessed.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGE(it) > 0.0
                                    } else {
                                        task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(it) > 0.0
                                    }
                                }
                                .mapIndexed { index, productInfo ->
                                    val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                                        productInfo.purchaseOrderUnits
                                    } else {
                                        productInfo.uom
                                    }
                                    val quantityNotProcessedProduct = if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                        task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGE(productInfo).toStringFormatted()
                                    } else {
                                        task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(productInfo).toStringFormatted()
                                    }
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countRefusalWithUom = "",
                                            quantityNotProcessedWithUom = "? $quantityNotProcessedProduct ${uom.name}",
                                            discrepanciesName = "",
                                            isNormDiscrepancies = false,
                                            productInfo = productInfo,
                                            productDiscrepancies = null,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                /**countNotProcessed.postValue(
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
                                .reversed())*/
            }

        }
    }

    private fun updateCountProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                countProcessed.postValue(
                        task.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies()
                                .filter {
                                    it.typeDiscrepancies != "1"
                                }
                                .mapIndexed { index, productDiscrepancies ->
                                    val productInfo = task.taskRepository.getProducts().findProduct(productDiscrepancies.materialNumber)
                                    val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                                        productInfo?.purchaseOrderUnits
                                    } else {
                                        productInfo?.uom
                                    }
                                    val discrepanciesName = qualityInfo.value?.findLast {
                                        it.code == productDiscrepancies.typeDiscrepancies
                                    }?.name
                                    val isNormDiscrepancies = when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                                        TaskListLoadingMode.PGE -> productDiscrepancies.typeDiscrepancies == "1" || productDiscrepancies.typeDiscrepancies == "2"
                                        else -> productDiscrepancies.typeDiscrepancies == "1"
                                    }
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo?.getMaterialLastSix()} ${productInfo?.description}",
                                            countRefusalWithUom = "${productDiscrepancies.numberDiscrepancies.toDouble().toStringFormatted()} ${uom?.name}",
                                            quantityNotProcessedWithUom = "",
                                            discrepanciesName = discrepanciesName ?: "",
                                            isNormDiscrepancies = isNormDiscrepancies,
                                            productInfo = productInfo,
                                            productDiscrepancies = productDiscrepancies,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }.reversed())
            } else {
                /**countProcessed.postValue(
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
                                .reversed()*/
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
        val matnr: String? = if (selectedPage.value == 0) {
            countNotProcessed.value?.get(position)?.productInfo?.materialNumber
        } else {
            countProcessed.value?.get(position)?.productInfo?.materialNumber
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false, isDiscrepancy = true)
    }

    fun onClickClean() {
        if (!isBatches.value!!) {
            processedSelectionsHelper.selectedPositions.value?.map { position ->
                if (!countProcessed.value?.get(position)!!.productInfo!!.isNotEdit) {
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.deleteProductDiscrepancy(countProcessed.value?.get(position)!!.productInfo!!.materialNumber, countProcessed.value?.get(position)!!.productDiscrepancies!!.typeDiscrepancies)

                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.deleteMercuryDiscrepancy(countProcessed.value?.get(position)!!.productInfo!!.materialNumber, countProcessed.value?.get(position)!!.productDiscrepancies!!.typeDiscrepancies)
                }

            }
        } else {
            /**processedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.deleteBatchesDiscrepanciesForBatch(countProcessed.value?.get(position)!!.batchInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getBatches()?.changeBatch(countProcessed.value?.get(position)!!.batchInfo!!.copy(isNoEAN = true))
            }*/
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
                    discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies().map { TaskProductDiscrepanciesRestData.from(it) },
                    discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies().map { TaskBatchesDiscrepanciesRestData.from(it) },
                    discrepanciesMercury = taskManager.getReceivingTask()!!.taskRepository.getMercuryDiscrepancies().getMercuryDiscrepancies().map { TaskMercuryDiscrepanciesRestData.from(it) }
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
