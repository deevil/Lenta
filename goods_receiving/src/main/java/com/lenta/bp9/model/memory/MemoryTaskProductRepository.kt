package com.lenta.bp9.model.memory

import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.repositories.ITaskProductRepository

class MemoryTaskProductRepository : ITaskProductRepository {

    private val productInfo: ArrayList<TaskProductInfo> = ArrayList()

    override fun getProducts(): List<TaskProductInfo> {
        return productInfo
    }

    override fun findProduct(product: TaskProductInfo): TaskProductInfo? {
        return findProduct(product.materialNumber)
    }

    override fun findProduct(materialNumber: String): TaskProductInfo? {
        return productInfo.firstOrNull { it.materialNumber == materialNumber}
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

    override fun changeProduct(product: TaskProductInfo): Boolean {
        deleteProduct(product)
        return addProduct(product)
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

    override fun clear() {
        productInfo.clear()
    }
}