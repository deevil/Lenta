package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessPerishablesService
@Inject constructor() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private lateinit var productInfo: TaskProductInfo

    fun newProcessPerishablesService(productInfo: TaskProductInfo) : ProcessPerishablesService? {
        return if (productInfo.type == ProductType.General){
            this.productInfo = productInfo.copy()
            this
        }
        else null
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