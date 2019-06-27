package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType

class StorplaceProcessing(val task: InventoryTask, val taskRepository: ITaskRepository, val taskDescription: TaskDescription, val storplaceNumber: String) {

    private lateinit var productBeforeProcessing: List<ProductInfo>
    private lateinit var NotProcessedProducts: List<ProductInfo>
    private lateinit var ProcessedProducts: List<ProductInfo>

    fun isDefaultStorplace() : Boolean {
        return true
    }

    fun isHasDataToDiscard() : Boolean {
        return true
    }

    fun processGeneralProduct(product: ProductInfo): ProcessGeneralProductService? {
        return if (product.type == ProductType.General || product.type == ProductType.NonExciseAlcohol) {
            ProcessGeneralProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun processExciseAlcoProduct(product: ProductInfo): ProcessExciseAlcoProductService? {
        return if (product.type == ProductType.ExciseAlcohol) {
            ProcessExciseAlcoProductService(taskDescription, taskRepository, product)
        } else null

    }

    fun getTotalCountOfProduct(product: ProductInfo): Double {
        return when (product.type) {
            ProductType.General -> processGeneralProduct(product)!!.getTotalCount()
            ProductType.NonExciseAlcohol -> processGeneralProduct(product)!!.getTotalCount()
            ProductType.ExciseAlcohol -> processExciseAlcoProduct(product)!!.getTotalCount()
            else -> 0.0
        }
    }

    fun addNewProduct( product: ProductInfo) : StorplaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearProduct( product: ProductInfo) : StorplaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun deleteProduct( product: ProductInfo) : StorplaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearBatchCount( product: ProductInfo) : StorplaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearExciseStamps( product: ProductInfo) : StorplaceProcessing {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun apply() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun discard() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}