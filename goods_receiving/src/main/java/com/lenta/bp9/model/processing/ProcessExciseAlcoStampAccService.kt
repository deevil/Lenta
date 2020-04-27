package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoStampAccService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private var productDiscrepancyInfo: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private lateinit var batchInfo: TaskBatchInfo
    private var batchDiscrepancyInfo: ArrayList<TaskBatchesDiscrepancies> = ArrayList()
    private val currentExciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()

    fun newProcessNonExciseAlcoProductService(productInfo: TaskProductInfo) : ProcessExciseAlcoStampAccService? {
        return if (productInfo.type == ProductType.ExciseAlcohol){
            this.productInfo = productInfo.copy()
            this.batchInfo = taskManager.getReceivingTask()!!.taskRepository.getBatches().findBatchOfProduct(productInfo)!!.copy()
            productDiscrepancyInfo.clear()
            batchDiscrepancyInfo.clear()
            currentExciseStamps.clear()
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
                            processingUnitNumber = "",
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
                            typeDiscrepancies = reasonRejectionCode,
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

    private fun addCuurentProduct(count: String, typeDiscrepancies: String){
        val foundDiscrepancy = productDiscrepancyInfo.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            processingUnitNumber = productInfo.processingUnit,
                            numberDiscrepancies = count,
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
                    changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = count, processingUnitNumber = productInfo.processingUnit))
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(isNoEAN = false))

        addBatch(count, typeDiscrepancies)
    }

    /**private fun addCuurentBatch(batchNumber: String, count: String, typeDiscrepancies: String){
        var index = -1
        for (i in batchDiscrepancyInfo.indices) {
            if (batchNumber.materialNumber == batchDiscrepancyInfo[i].materialNumber) {
                index = i
            }
        }

        val foundBatchDiscrepancy = batchDiscrepancyInfo.findLast {
            it.materialNumber == batchInfo.materialNumber && it.batchNumber == batchInfo.batchNumber && it.typeDiscrepancies == typeDiscrepancies
        }

        if (foundBatchDiscrepancy == null) {
            batchDiscrepancyInfo.
                    add(TaskBatchesDiscrepancies(
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
            val numberDiscrepancies = foundBatchDiscrepancy.numberDiscrepancies
            batchDiscrepancyInfo.remove(foundBatchDiscrepancy)
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getBatchesDiscrepancies()?.
                    changeBatchDiscrepancy(foundBatchDiscrepancy.copy(numberDiscrepancies = count))
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getBatches()?.
                changeBatch(batchInfo.copy(isNoEAN = false))
    }

    fun addCurrentExciseStamp(exciseStamp: TaskExciseStamp){
        productInfo = productInfo!!.copy(factCount = productInfo!!.factCount + 1)
        currentExciseStamps.add(exciseStamp)
    }*/

    fun overlimit(count: Double) : Boolean {
        return productInfo.numberStampsControl.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)

    }

    fun getCountExciseStamps(): Int {
        return currentExciseStamps.size
    }

    fun isTaskAlreadyHasExciseStamp(stampCode: String): Boolean{
        return currentExciseStamps.any { currExciseStamp ->
            currExciseStamp.code == stampCode
        } || taskManager.getReceivingTask()!!.taskRepository.getExciseStamps().getExciseStamps().any {repExciseStamp ->
            repExciseStamp.code == stampCode
        }
    }
}