package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.ReceivingProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<ReceivingProductInfo>
    fun findProduct(product: ReceivingProductInfo): ReceivingProductInfo?
    fun findProduct(materialNumber: String): ReceivingProductInfo?
    fun addProduct(product: ReceivingProductInfo): Boolean
    fun changeProduct(product: ReceivingProductInfo): Boolean
    fun deleteProduct(product: ReceivingProductInfo): Boolean
    fun clear()
}