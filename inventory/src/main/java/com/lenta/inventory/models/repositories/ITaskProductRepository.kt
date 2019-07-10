package com.lenta.inventory.models.repositories

import com.lenta.inventory.models.task.TaskProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<TaskProductInfo>
    fun findProduct(product: TaskProductInfo): TaskProductInfo?
    fun findProduct(materialNumber: String, storePlaceNumber: String): TaskProductInfo?
    fun addProduct(product: TaskProductInfo): Boolean
    fun deleteProduct(product: TaskProductInfo)
    fun getNotProcessedProducts(): List<TaskProductInfo>
    fun getProcessedProducts(): List<TaskProductInfo>
    fun clear()
}