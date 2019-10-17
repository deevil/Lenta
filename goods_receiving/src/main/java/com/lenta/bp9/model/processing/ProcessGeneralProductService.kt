package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessGeneralProductService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    private lateinit var productInfo: TaskProductInfo

    fun newProcessGeneralProductService(productInfo: TaskProductInfo) : ProcessGeneralProductService? {
        return if (productInfo.type == ProductType.General){
            this.productInfo = productInfo.copy()
            this
        }
        else null
    }

    fun countEqualOrigQuantity (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() == ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun countLargerOrigQuantity (count: Double) : Boolean {
        return productInfo.origQuantity.toDouble() > ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0) + count)
    }

    fun getRoundingQuantity (origQuantity: Double) : Double {
        return origQuantity - ((taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo) ?: 0.0)
                + (taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo) ?: 0.0))
    }

    fun paramGrsGrundNeg (paramGrsGrundNeg: String) : Boolean {
        return !taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.filter {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == paramGrsGrundNeg
        }.isNullOrEmpty()
    }

    fun add(count: String, reasonRejectionCode: String){
        val foundDiscrepancy = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.findProductDiscrepanciesOfProduct(productInfo)?.findLast {
            it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == reasonRejectionCode
        }

        if (foundDiscrepancy == null) {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(TaskProductDiscrepancies(
                            materialNumber = productInfo.materialNumber,
                            exidv = "",
                            numberDiscrepancies = count,
                            uom = productInfo.uom,
                            typeDiscrepancies = reasonRejectionCode,
                            isNotEdit = false,
                            isNew = false
                    ))
        } else {
            taskManager.getReceivingTask()?.
                    taskRepository?.
                    getProductsDiscrepancies()?.
                    changeProductDiscrepancy(foundDiscrepancy.copy(numberDiscrepancies = count))
        }

        taskManager.getReceivingTask()?.
                taskRepository?.
                getProducts()?.
                changeProduct(productInfo.copy(isNoEAN = false))
    }
}