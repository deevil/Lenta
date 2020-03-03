package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import javax.inject.Inject

@AppScope
class ProcessNonExciseAlcoProductService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private lateinit var batchInfo: TaskBatchInfo

    fun newProcessNonExciseAlcoProductService(productInfo: TaskProductInfo) : ProcessNonExciseAlcoProductService? {
        return if (productInfo.type == ProductType.NonExciseAlcohol){
            this.productInfo = productInfo.copy()
            this.batchInfo = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo)!!.copy()
            this
        }
        else null
    }

    private fun getCountOfDiscrepancies(typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountOfDiscrepanciesOfProduct(productInfo, typeDiscrepancies)
    }

    fun add(count: String, typeDiscrepancies: String){
        val countAdd = if (typeDiscrepancies == "1") count.toDouble() else getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            exidv = "",
                            numberDiscrepancies = countAdd.toString(),
                            uom = productInfo.uom,
                            typeDiscrepancies = typeDiscrepancies,
                            isNotEdit = false,
                            isNew = false
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(isNoEAN = false))

        addBatch(countAdd.toString(), typeDiscrepancies)
    }

    private fun addBatch(count: String, typeDiscrepancies: String){
        val foundBatchDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.findBatchDiscrepanciesOfBatch(batchInfo)?.findLast {
            it.materialNumber == batchInfo.materialNumber /**&& it.batchNumber == batchInfo.batchNumber*/ && it.typeDiscrepancies == typeDiscrepancies
        }

        /**if (foundBatchDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getBatchesDiscrepancies()?.
                    changeBatchDiscrepancy(TaskBatchesDiscrepancies(
                            materialNumber = batchInfo.materialNumber,
                            batchNumber = batchInfo.batchNumber,
                            numberDiscrepancies = count,
                            uom = batchInfo.uom,
                            typeDiscrepancies = typeDiscrepancies,
                            isNotEdit = false,
                            exciseStampCode = "",
                            fullDM = ""
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getBatchesDiscrepancies()?.
                    changeBatchDiscrepancy(foundBatchDiscrepancy.copy(numberDiscrepancies = count))
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getBatches()?.
                changeBatch(batchInfo.copy(isNoEAN = false))*/
    }

    fun overlimit(count: Double) : Boolean {
        return false /**batchInfo.planQuantityBatch.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)*/

    }
}