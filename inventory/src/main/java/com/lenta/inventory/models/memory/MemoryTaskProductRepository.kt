package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.inventory.models.task.TaskProductInfo
import java.util.*
import kotlin.collections.ArrayList

class MemoryTaskProductRepository : ITaskProductRepository {

    private val productInfo: ArrayList<TaskProductInfo> = ArrayList()

    override fun getProducts(): List<TaskProductInfo> {
        return productInfo.toList()
    }

    override fun findProduct(product: TaskProductInfo): TaskProductInfo? {
        return findProduct(product.materialNumber)
    }

    override fun findProduct(materialNumber: String): TaskProductInfo? {
        return productInfo.firstOrNull { it.materialNumber == materialNumber }
    }

    override fun addProduct(product: TaskProductInfo): Boolean {
        var index = -1
        for (i in productInfo.indices) {
            if (product.materialNumber == productInfo[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            productInfo.add(product)
            return true
        } else if (index !=  productInfo.size - 1) {
            productInfo.getOrNull(index)?.let {
                productInfo.removeAt(index)
                productInfo.add(it)
            }

        }

        return false
    }

    override fun deleteProduct(product: TaskProductInfo): Boolean {
        var index = -1
        for (i in productInfo.indices) {
            if (product.materialNumber == productInfo[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        productInfo.removeAt(index)
        return true
    }

    override fun getNotProcessedProducts(): List<TaskProductInfo> {
        var notProcessedProducts = ArrayList<TaskProductInfo>()
        productInfo.forEach { taskProductInfo: TaskProductInfo ->
            if (!taskProductInfo.isPositionCalc) notProcessedProducts.add(taskProductInfo)
        }
        return notProcessedProducts
    }

    override fun getProcessedProducts(): List<TaskProductInfo> {
        var processedProducts = ArrayList<TaskProductInfo>()
        productInfo.forEach { taskProductInfo: TaskProductInfo ->
            if (taskProductInfo.isPositionCalc) processedProducts.add(taskProductInfo)
        }
        return processedProducts
    }

    override fun clear() {
        productInfo.clear()
    }

}