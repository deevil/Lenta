package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessNonExciseAlcoProductPGEService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo

    fun newProcessNonExciseAlcoProductPGEService(productInfo: TaskProductInfo) : ProcessNonExciseAlcoProductPGEService? {
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

    fun add(count: String, typeDiscrepancies: String, processingUnit: String, batchInfo: TaskBatchInfo){
        val countAdd = getCountOfDiscrepancies(typeDiscrepancies, processingUnit) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancyOfProcessingUnit(TaskProductDiscrepancies(
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
            changeProductDiscrepancyOfProcessingUnit(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }

        taskManager.getReceivingTask()?.
        taskRepository?.
        getProducts()?.
        changeProduct(productInfo.copy(isNoEAN = false))

        addBatch(count, typeDiscrepancies, processingUnit, batchInfo)
    }

    private fun addBatch(count: String, typeDiscrepancies: String, processingUnit: String, batchInfo: TaskBatchInfo) {
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
                    processingUnitNumber = processingUnit,
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
            changeBatchDiscrepancy(foundBatchDiscrepancy.copy(numberDiscrepancies = countAdd.toString(), processingUnitNumber = processingUnit))
        }
    }

    fun addSurplus(count: String, typeDiscrepancies: String, processingUnit: String, batchInfo: TaskBatchInfo) { //https://trello.com/c/P9KBZcNB
        val countAdd = batchInfo.purchaseOrderScope - getQuantityAllCategoryProductPGE(batchInfo)
        add(countAdd.toString(), typeDiscrepancies, processingUnit, batchInfo)
        val countAddSurplus = count.toDouble() - countAdd
        add(countAddSurplus.toString(), "2", processingUnit, batchInfo)
    }

    fun addGoodsAddedAsSurplus(count: String, typeDiscrepancies: String, processingUnit: String){
        val countAdd = getCountOfDiscrepancies(typeDiscrepancies, processingUnit) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancyOfProcessingUnit(TaskProductDiscrepancies(
                    materialNumber = productInfo.materialNumber,
                    processingUnitNumber = processingUnit,
                    numberDiscrepancies = countAdd.toString(),
                    uom = productInfo.uom,
                    typeDiscrepancies = typeDiscrepancies,
                    isNotEdit = false,
                    isNew = productInfo.isGoodsAddedAsSurplus,
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

    }

    private fun getQuantityAllCategoryProductPGE(batchInfo: TaskBatchInfo) : Double {
        return (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batchInfo) ?: 0.0 ) +
                (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo) ?: 0.0 )
    }

    fun overLimit(count: Double, batchInfo: TaskBatchInfo) : Boolean {
        return batchInfo.purchaseOrderScope < ((taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batchInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo) ?: 0.0) + count)

    }
}