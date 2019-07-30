package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class ProcessGeneralProductService@Inject constructor() : IProcessProductService{

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private val taskRepository: ITaskRepository by lazy {
        processServiceManager.getInventoryTask()!!.taskRepository!!
    }
    private lateinit var productInfo: TaskProductInfo

    fun newProcessGeneralProductService(productInfo: TaskProductInfo) : ProcessGeneralProductService {
        this.productInfo = productInfo
        return this
    }

    override fun getFactCount(): Double {
        return productInfo.factCount
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