package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class ProcessGeneralProductService(private val taskDescription: TaskDescription,
                                   private val taskRepository: ITaskRepository,
                                   private val productInfo: TaskProductInfo) : IProcessProductService {
    override fun getTotalCount(): Double {
        return productInfo.factCount ?: 0.0
    }

    override fun apply(): InventoryTask {
        return InventoryTask(taskDescription, taskRepository)
    }

    override fun discard(): InventoryTask {
        return InventoryTask(taskDescription, taskRepository)
    }

    fun addCount(count: Double){
        if (count >= 0.0) {
            if (count > 0.0) {
                productInfo.factCount = count
                productInfo.isPositionCalc = true
            } else {
                productInfo.factCount = 0.0
                productInfo.isPositionCalc = false
            }
        }
    }

    fun missing(){
        productInfo.factCount = 0.0
        productInfo.isPositionCalc = true
    }
}