package com.lenta.inventory.models.task

import com.lenta.shared.di.AppScope
import com.lenta.shared.models.core.ProductType
import javax.inject.Inject

@AppScope
class ProcessGeneralProductService@Inject constructor() : IProcessProductService{

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    private lateinit var productInfo: TaskProductInfo

    fun newProcessGeneralProductService(productInfo: TaskProductInfo) : ProcessGeneralProductService? {
        return if (productInfo.type == ProductType.General || productInfo.type == ProductType.NonExciseAlcohol){
            this.productInfo = productInfo
            this
        }
        else null
    }

    override fun getFactCount(): Double {
        return productInfo.factCount
    }

    override fun setFactCount(count: Double){
        if (count >= 0.0) {
            if (count > 0.0) {
                processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.factCount = count
                processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
            } else {
                processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
                processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = false
            }
        }
    }

    override fun markMissing(){
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.factCount = 0.0
        processServiceManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(productInfo)?.isPositionCalc = true
    }
}