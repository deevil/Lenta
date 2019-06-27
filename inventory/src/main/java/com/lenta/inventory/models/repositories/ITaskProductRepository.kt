package com.lenta.inventory.models.repositories

import com.lenta.shared.models.core.ProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<ProductInfo>
    fun findProduct(product: ProductInfo): ProductInfo?
    fun findProduct(materialNumber: String): ProductInfo?
    fun addProduct(product: ProductInfo): Boolean
    fun deleteProduct(product: ProductInfo): Boolean
    fun clear()
    operator fun get(index: Int): ProductInfo
}