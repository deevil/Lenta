package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import javax.inject.Inject

@AppScope
class ProcessNonExciseAlcoProductPGEService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private lateinit var batchInfo: TaskBatchInfo

    fun newProcessNonExciseAlcoProductPGEService(productInfo: TaskProductInfo) : ProcessNonExciseAlcoProductPGEService? {
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

    fun add(count: String, typeDiscrepancies: String, processingUnit: String){
        val countAdd = getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancy(TaskProductDiscrepancies(
                    materialNumber = productInfo.materialNumber,
                    processingUnitNumber = processingUnit,
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
            changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString(), processingUnitNumber = processingUnit))
        }

        taskManager.getReceivingTask()?.
        taskRepository?.
        getProducts()?.
        changeProduct(productInfo.copy(isNoEAN = false))

        addBatch(countAdd.toString(), typeDiscrepancies, processingUnit)
    }

    private fun addBatch(count: String, typeDiscrepancies: String, processingUnit: String) {
        val foundBatchDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.findBatchDiscrepanciesOfBatch(batchInfo)?.findLast {
            it.materialNumber == batchInfo.materialNumber && it.batchNumber == batchInfo.batchNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundBatchDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getBatchesDiscrepancies()?.
            changeBatchDiscrepancy(TaskBatchesDiscrepancies(
                    materialNumber = batchInfo.materialNumber,
                    processingUnitNumber = processingUnit,
                    batchNumber = batchInfo.batchNumber,
                    numberDiscrepancies = count,
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
            changeBatchDiscrepancy(foundBatchDiscrepancy.copy(numberDiscrepancies = count, processingUnitNumber = processingUnit))
        }
    }

    fun addSurplus(count: String, typeDiscrepancies: String, processingUnit: String) { //https://trello.com/c/P9KBZcNB
        val countAdd = batchInfo.purchaseOrderScope - getQuantityAllCategoryProductPGE()
        add(countAdd.toString(), typeDiscrepancies, processingUnit)
        val countAddSurplus = count.toDouble() - countAdd
        add(countAddSurplus.toString(), "2", processingUnit)
    }

    private fun getQuantityAllCategoryProductPGE() : Double {
        return (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batchInfo) ?: 0.0 )+
                (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo) ?: 0.0 )
    }

    fun overlimit(count: Double) : Boolean {
        return batchInfo.purchaseOrderScope < ((taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batchInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo) ?: 0.0) + count)

    }
}