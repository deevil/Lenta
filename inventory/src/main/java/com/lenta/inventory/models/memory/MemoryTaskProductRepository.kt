package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.shared.models.core.ProductInfo
import java.util.ArrayList

class MemoryTaskProductRepository(private val productInfo: ArrayList<ProductInfo> = ArrayList()) : ITaskProductRepository {
    override fun getProducts(): List<ProductInfo> {
        return productInfo.toList()
    }

    override fun findProduct(product: ProductInfo): ProductInfo? {
        return findProduct(product.materialNumber)
    }

    override fun findProduct(materialNumber: String): ProductInfo? {
        return productInfo.firstOrNull { it.materialNumber == materialNumber }
    }

    override fun addProduct(product: ProductInfo): Boolean {
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

    override fun deleteProduct(product: ProductInfo): Boolean {
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

    override fun get(index: Int): ProductInfo {
        return productInfo[index]
    }

    override fun lenght(): Int {
        return productInfo.size
    }
}