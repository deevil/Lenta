package com.lenta.inventory.models.memory

import android.os.Build
import androidx.annotation.RequiresApi
import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.shared.utilities.Logg

class MemoryTaskProductRepository : ITaskProductRepository {

    private val productInfo: ArrayList<TaskProductInfo> = ArrayList()

    override fun getProducts(): List<TaskProductInfo> {
        return productInfo.toList()
    }

    override fun findProduct(product: TaskProductInfo): TaskProductInfo? {
        return findProduct(product.materialNumber, product.placeCode)
    }

    override fun findProduct(materialNumber: String, storePlaceNumber: String): TaskProductInfo? {
        return productInfo.firstOrNull { it.materialNumber == materialNumber && it.placeCode == storePlaceNumber}
    }

    override fun updateProducts(newProducts: List<TaskProductInfo>) {
        for (productInfo in newProducts) {
            addProduct(productInfo)
        }
    }

    override fun changeProduct(product: TaskProductInfo) {
        deleteProduct(product)
        addProduct(product)
    }

    override fun addProduct(product: TaskProductInfo): Boolean {
        var index = -1
        for (i in productInfo.indices) {
            if (product.materialNumber == productInfo[i].materialNumber && product.placeCode == productInfo[i].placeCode) {
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
            if (product.materialNumber == productInfo[i].materialNumber && product.placeCode == productInfo[i].placeCode) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        productInfo.removeAt(index)
        return true
    }

    override fun getNotProcessedProducts(storePlaceNumber: String?): List<TaskProductInfo> {
        return productInfo.filter { (storePlaceNumber == null || it.placeCode == storePlaceNumber) && !it.isPositionCalc && !it.isDel }
    }
    override fun getProcessedProducts(storePlaceNumber: String?): List<TaskProductInfo> {
        return productInfo.filter { (storePlaceNumber == null || it.placeCode == storePlaceNumber) && it.isPositionCalc && !it.isDel }
    }

    override fun clear() {
        productInfo.clear()
    }

}