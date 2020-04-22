package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.*
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessExciseAlcoBoxAccService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo
    private var productDiscrepancyInfo: ArrayList<TaskProductDiscrepancies> = ArrayList()
    private lateinit var batchInfo: TaskBatchInfo
    private var batchDiscrepancyInfo: ArrayList<TaskBatchesDiscrepancies> = ArrayList()
    private val currentExciseStamps: ArrayList<TaskExciseStampInfo> = ArrayList()

    fun newProcessNonExciseAlcoProductService(productInfo: TaskProductInfo) : ProcessExciseAlcoBoxAccService? {
        return if (productInfo.type == ProductType.ExciseAlcohol && productInfo.isBoxFl){ //алкоголь, коробочный учет https://trello.com/c/KbBbXj2t
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

    }

    fun overLimit(count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() < ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)

    }

    fun searchExciseStamp(code: String) : TaskExciseStampInfo? {
        return taskManager.getReceivingTask()?.taskRepository?.getExciseStamps()?.getExciseStamps()?.findLast {
            it.code == code
        }
    }

    fun searchBox(code: String) {

    }

}