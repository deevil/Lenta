package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskProductRepository : ITaskProductRepository {

    private val productInfo = ArrayList<ProductInfo>()

    override fun getProducts(): List<ProductInfo> {
        return productInfo
    }

    override fun findProduct(product: ProductInfo): ProductInfo? {
        for (i in productInfo.indices) {
            if (product.materialNumber === productInfo.get(i).materialNumber) {
                return productInfo[i]
            }
        }

        return null
    }

    override fun addProduct(product: ProductInfo): Boolean {
        var index = -1
        for (i in productInfo.indices) {
            if (product.materialNumber === productInfo.get(i).materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            productInfo.add(product)
            return true
        }

        return false
    }

    override fun deleteProduct(product: ProductInfo): Boolean {
        var index = -1
        for (i in productInfo.indices) {
            if (product.materialNumber === productInfo.get(i).materialNumber) {
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
        return productInfo.get(index)
    }

    override fun lenght(): Int {
        return productInfo.size
    }
}