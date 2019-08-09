package com.lenta.inventory.models.repositories

import com.lenta.inventory.models.task.TaskProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<TaskProductInfo>
    fun findProduct(product: TaskProductInfo): TaskProductInfo?
    fun findProduct(materialNumber: String, storePlaceNumber: String): TaskProductInfo?
    fun addProduct(product: TaskProductInfo): Boolean
    fun changeProduct(product: TaskProductInfo)
    fun updateProducts(newProducts: List<TaskProductInfo>)
    fun deleteProduct(product: TaskProductInfo): Boolean
    fun getNotProcessedProducts(storePlaceNumber: String? = null): List<TaskProductInfo>
    fun getProcessedProducts(storePlaceNumber: String? = null): List<TaskProductInfo>
    fun clear()
}