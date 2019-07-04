package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.inventory.models.task.TaskProductInfo

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
        return productInfo.filter { !it.isPositionCalc }
    }

    override fun getProcessedProducts(): List<TaskProductInfo> {
        return productInfo.filter { it.isPositionCalc }
    }

    override fun clear() {
        productInfo.clear()
    }

}