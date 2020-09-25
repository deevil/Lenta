package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskProductRepository(
        private val products: ArrayList<ProductInfo> = ArrayList()
) : ITaskProductRepository {

    override fun getProducts(): List<ProductInfo> {
        return products.toList()
    }

    override fun findProduct(product: ProductInfo): ProductInfo? {
        return findProduct(product.materialNumber)
    }

    override fun findProduct(materialNumber: String): ProductInfo? {
        return products.firstOrNull { it.materialNumber == materialNumber }
    }

    override fun addProduct(product: ProductInfo): Boolean {
        var index = -1
        for (i in products.indices) {
            if (product.materialNumber == products[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            products.add(product)
            return true
        } else if (index !=  products.size - 1) {
            products.getOrNull(index)?.let {
                products.removeAt(index)
                products.add(it)
            }

        }

        return false
    }

    override fun deleteProduct(product: ProductInfo): Boolean {
        var index = -1
        for (i in products.indices) {
            if (product.materialNumber == products[i].materialNumber) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        products.removeAt(index)
        return true
    }

    override fun clear() {
        products.clear()
    }

    override fun get(index: Int): ProductInfo {
        return products[index]
    }

    override fun lenght(): Int {
        return products.size
    }
}