package com.lenta.bp10.models.repositories

import com.lenta.shared.models.core.ProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<ProductInfo>
    fun findProduct(product: ProductInfo): ProductInfo
    fun addProduct(product: ProductInfo): Boolean
    fun deleteProduct(product: ProductInfo): Boolean
    fun clear()
    operator fun get(index: Int): ProductInfo
    fun lenght(): Int
}