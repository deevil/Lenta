package com.lenta.bp9.features.goods_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val batchInfo: MutableLiveData<TaskBatchInfo> = MutableLiveData()
    val goodsDetails: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    fun onResume() {
        viewModelScope.launch {
            reasonRejectionInfo.value = dataBase.getAllReasonRejectionInfo()
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
        goodsDetails.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo.value!!)?.mapIndexed { index, discrepancy ->
                    GoodsDetailsCategoriesItem(
                            number = index + 1,
                            name = "${reasonRejectionInfo.value?.firstOrNull {it.code == discrepancy.typeDiscrepancies}?.name}",
                            quantity = discrepancy.numberDiscrepancies,
                            typeDiscrepancies = discrepancy.typeDiscrepancies,
                            uomName = discrepancy.uom.name,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }

    private fun updateBatch() {
        goodsDetails.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.findBatchDiscrepanciesOfBatch(batchInfo.value!!)?.mapIndexed { index, discrepancy ->
                    GoodsDetailsCategoriesItem(
                            number = index + 1,
                            name = "${reasonRejectionInfo.value?.firstOrNull {it.code == discrepancy.typeDiscrepancies}?.name}",
                            quantity = discrepancy.numberDiscrepancies,
                            typeDiscrepancies = discrepancy.typeDiscrepancies,
                            uomName = discrepancy.uom.name,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
    }
}
