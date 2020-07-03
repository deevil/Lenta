package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessNonExciseSetsReceivingProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo
    private var countSet: Double = 0.0

    fun newProcessNonExciseSetsReceivingProductService(productInfo: TaskProductInfo) : ProcessNonExciseSetsReceivingProductService? {
        return if (productInfo.type == ProductType.NonExciseAlcohol && productInfo.isSet){
            this.productInfo = productInfo.copy()
            this
        }
        else null
    }

    private fun getCountOfDiscrepancies(typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountOfDiscrepanciesOfProduct(productInfo, typeDiscrepancies) ?: 0.0
    }

    private fun getCountOfDiscrepanciesOfBatch(batchInfo: TaskBatchInfo, typeDiscrepancies: String) : Double {
        return taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getCountOfDiscrepanciesOfBatch(batchInfo, typeDiscrepancies)
    }

    fun overLimit(count: Double) : Boolean {
    return productInfo.origQuantity.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
    + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun overLimitBatch(count: Double, batchInfo: TaskBatchInfo) : Boolean {
        return batchInfo.purchaseOrderScope < ((taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountAcceptOfBatchPGE(batchInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo) ?: 0.0) + count)

    }

    fun addSet(count: String, typeDiscrepancies: String){
        val countAdd: Double = getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancy(TaskProductDiscrepancies(
                    materialNumber = productInfo.materialNumber,
                    processingUnitNumber = productInfo.processingUnit,
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
            changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
        }
    }

    fun addComponent(count: String, typeDiscrepancies: String, componentNumber: String, batchInfo: TaskBatchInfo){
        val countAdd = getCountOfDiscrepancies(typeDiscrepancies) + count.toDouble()
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(componentNumber)?.findLast {
            it.materialNumber == componentNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
            taskRepository?.
            getProductsDiscrepancies()?.
            changeProductDiscrepancy(TaskProductDiscrepancies(
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
            changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = countAdd.toString()))
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

    fun setCountSet(count: Double) {
        countSet = count
    }

    fun getCountSet() : Double {
        return countSet
    }
}