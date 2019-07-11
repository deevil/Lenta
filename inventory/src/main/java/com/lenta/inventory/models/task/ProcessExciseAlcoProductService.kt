package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class ProcessExciseAlcoProductService(val taskDescription: TaskDescription,
                                      val taskRepository: ITaskRepository,
                                      val productInfo: TaskProductInfo) : IProcessProductService {
    override fun getFactCount(): Double {
        return taskRepository.getProducts().findProduct(productInfo)?.factCount ?: 0.0
    }

    override fun setFactCount(count: Double){
        if (count >= 0.0) {
            if (count > 0.0) {
                taskRepository.getProducts().findProduct(productInfo)?.factCount = count
                taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
            } else {
                taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
                taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = false
            }
        }
    }

    override fun markMissing(){
        taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
        taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
    }
}