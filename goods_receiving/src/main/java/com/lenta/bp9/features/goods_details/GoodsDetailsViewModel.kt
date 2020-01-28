package com.lenta.bp9.features.goods_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var processMercuryProductService: ProcessMercuryProductService
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val uom: MutableLiveData<Uom?> by lazy {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
            MutableLiveData(productInfo.value?.purchaseOrderUnits)
        } else {
            MutableLiveData(productInfo.value?.uom)
        }
    }
    val batchInfo: MutableLiveData<TaskBatchInfo> = MutableLiveData()
    val goodsDetails: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        MutableLiveData(repoInMemoryHolder.taskList.value?.taskListLoadingMode == TaskListLoadingMode.PGE)
    }
    val isVetProduct: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isVet ?: false)
    }
    val categoriesSelectionsHelper = SelectionItemsHelper()
    val enabledDelBtn: MutableLiveData<Boolean> = categoriesSelectionsHelper.selectedPositions.map {
        !categoriesSelectionsHelper.selectedPositions.value.isNullOrEmpty()
    }

    init {
        viewModelScope.launch {
            reasonRejectionInfo.value = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                dataBase.getQualityInfoPGE()?.map {
                    ReasonRejectionInfo(
                            id = it.id,
                            qualityCode = "",
                            code = it.code,
                            name = it.name
                    )
                }
            } else {
                dataBase.getAllReasonRejectionInfo()
            }
            if (productInfo.value != null) {
                updateProduct()
            } else {
                if (batchInfo.value != null) {
                    updateBatch()
                }
            }
        }

    }

    private fun updateProduct() {
        if (isVetProduct.value!! && !productInfo.value!!.isNotEdit) {
            goodsDetails.postValue(
                    processMercuryProductService.getGoodsDetails()?.mapIndexed { index, discrepancy ->
                        val isNormDiscrepancies = when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                            TaskListLoadingMode.PGE -> discrepancy.typeDiscrepancies == "1" || discrepancy.typeDiscrepancies == "2"
                            else -> discrepancy.typeDiscrepancies == "1"
                        }
                        GoodsDetailsCategoriesItem(
                                number = index + 1,
                                name = "${reasonRejectionInfo.value?.firstOrNull {it.code == discrepancy.typeDiscrepancies}?.name}",
                                quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name}",
                                isNormDiscrepancies = isNormDiscrepancies,
                                typeDiscrepancies = discrepancy.typeDiscrepancies,
                                materialNumber = productInfo.value?.materialNumber ?: "",
                                batchNumber = "",
                                even = index % 2 == 0
                        )
                    }?.reversed()
            )
        } else {
            goodsDetails.postValue(
                    taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo.value!!)?.mapIndexed { index, discrepancy ->
                        val isNormDiscrepancies = when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                            TaskListLoadingMode.PGE -> discrepancy.typeDiscrepancies == "1" || discrepancy.typeDiscrepancies == "2"
                            else -> discrepancy.typeDiscrepancies == "1"
                        }
                        GoodsDetailsCategoriesItem(
                                number = index + 1,
                                name = "${reasonRejectionInfo.value?.firstOrNull {it.code == discrepancy.typeDiscrepancies}?.name}",
                                quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name}",
                                isNormDiscrepancies = isNormDiscrepancies,
                                typeDiscrepancies = discrepancy.typeDiscrepancies,
                                materialNumber = productInfo.value?.materialNumber ?: "",
                                batchNumber = "",
                                even = index % 2 == 0
                        )
                    }?.reversed()
            )
        }
    }

    private fun updateBatch() {
        goodsDetails.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.findBatchDiscrepanciesOfBatch(batchInfo.value!!)?.mapIndexed { index, discrepancy ->
                    val isNormDiscrepancies = when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                        TaskListLoadingMode.PGE -> discrepancy.typeDiscrepancies == "1" || discrepancy.typeDiscrepancies == "2"
                        else -> discrepancy.typeDiscrepancies == "1"
                    }
                    GoodsDetailsCategoriesItem(
                            number = index + 1,
                            name = "${reasonRejectionInfo.value?.firstOrNull {it.code == discrepancy.typeDiscrepancies}?.name}",
                            quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${discrepancy.uom.name}",
                            isNormDiscrepancies = isNormDiscrepancies,
                            typeDiscrepancies = discrepancy.typeDiscrepancies,
                            materialNumber = "",
                            batchNumber = batchInfo.value?.batchNumber ?: "",
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }

    fun onClickDelete() {
        if (productInfo.value != null && !productInfo.value!!.isNotEdit) {
            categoriesSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.deleteProductDiscrepancy(goodsDetails.value?.get(position)!!.materialNumber, goodsDetails.value?.get(position)!!.typeDiscrepancies)

                if (isVetProduct.value!!) {
                    processMercuryProductService.delDiscrepancy(goodsDetails.value?.get(position)!!.typeDiscrepancies)
                }
            }
            updateProduct()
        }
    }
}
