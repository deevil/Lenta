package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessNonExciseAlcoProductService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo

    fun newProcessNonExciseAlcoProductService(productInfo: TaskProductInfo) : ProcessNonExciseAlcoProductService? {
        return if (productInfo.type == ProductType.NonExciseAlcohol){
            this.productInfo = productInfo.copy()
            this
        }
        else null
    }

    private fun getCountOfDiscrepancies(typeDiscrepancies: String, processingUnitNumber: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountOfDiscrepanciesOfProductOfProcessingUnit(productInfo, typeDiscrepancies, processingUnitNumber)
    }

    private fun getCountOfDiscrepanciesOfBatch(batchInfo: TaskBatchInfo, typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getCountOfDiscrepanciesOfBatch(batchInfo, typeDiscrepancies)
    }

    fun add(count: String, typeDiscrepancies: String, batchInfo: TaskBatchInfo){
        val countAdd = getCountOfDiscrepancies(typeDiscrepancies, productInfo.processingUnit) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancyOfProcessingUnit(TaskProductDiscrepancies(
                    materialNumber = productInfo.materialNumber,
                    processingUnitNumber = productInfo.processingUnit,
                    numberDiscrepancies = countAdd.toString(),
                    uom = productInfo.uom,
                    typeDiscrepancies = typeDiscrepancies,
                    isNotEdit = false,
                    isNew = false,
                    notEditNumberDiscrepancies = ""
            ))
        } else {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }

        taskManager.getReceivingTask()?.
        taskRepository?.
        getProducts()?.
        changeProduct(productInfo.copy(isNoEAN = false))

        addBatch(count, typeDiscrepancies, batchInfo)
    }

    private fun addBatch(count: String, typeDiscrepancies: String, batchInfo: TaskBatchInfo){
        val countAdd = getCountOfDiscrepanciesOfBatch(batchInfo, typeDiscrepancies) + count.toDouble()
        val foundBatchDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.findBatchDiscrepanciesOfBatch(batchInfo)?.findLast {
            it.materialNumber == batchInfo.materialNumber && it.batchNumber == batchInfo.batchNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundBatchDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getBatchesDiscrepancies()?.
            changeBatchDiscrepancy(TaskBatchesDiscrepancies(
                    materialNumber = batchInfo.materialNumber,
                    processingUnitNumber = batchInfo.processingUnitNumber,
                    batchNumber = batchInfo.batchNumber,
                    numberDiscrepancies = countAdd.toString(),
                    uom = productInfo.uom,
                    typeDiscrepancies = typeDiscrepancies,
                    isNotEdit = false,
                    isNew = false,
                    setMaterialNumber = batchInfo.setMaterialNumber,
                    egais = batchInfo.egais,
                    bottlingDate = batchInfo.bottlingDate,
                    notEditNumberDiscrepancies = ""
            ))
        } else {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getBatchesDiscrepancies()?.
            changeBatchDiscrepancy(foundBatchDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }
    }

    fun overLimit(count: Double, batchInfo: TaskBatchInfo) : Boolean {
        return batchInfo.purchaseOrderScope < ((taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatch(batchInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batchInfo) ?: 0.0) + count)

    }
}