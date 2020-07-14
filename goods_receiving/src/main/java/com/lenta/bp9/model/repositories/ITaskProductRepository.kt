package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskProductRepository {
    fun getProducts(): List<TaskProductInfo>
    fun findProduct(product: TaskProductInfo): TaskProductInfo?
    fun findProduct(materialNumber: String): TaskProductInfo?
    fun addProduct(product: TaskProductInfo): Boolean
    fun updateProducts(newProducts: List<TaskProductInfo>)
    fun changeProduct(product: TaskProductInfo): Boolean
    fun deleteProduct(delProduct: TaskProductInfo): Boolean
    fun getProcessingUnitsOfProduct(materialNumber: String): List<TaskProductInfo>
    fun clear()
}