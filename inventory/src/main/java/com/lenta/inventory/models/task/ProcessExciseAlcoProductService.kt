package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.shared.models.core.EgaisStampVersion

class ProcessExciseAlcoProductService(val taskDescription: TaskDescription,
                                      val taskRepository: ITaskRepository,
                                      val productInfo: TaskProductInfo) : IProcessProductService {

    override fun getFactCount(): Double {
        return  taskRepository.getProducts().findProduct(productInfo).let {taskProductInfo ->
            (taskProductInfo!!.factCount ?: 0.0) +
            taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo).filter {
                it.code.length == EgaisStampVersion.V3.version
            }.size.toDouble()
        }
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