package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskProductsDiscrepanciesRepository {
    fun getProductsDiscrepancies(): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies>
    fun addProductDiscrepancies(discrepancies: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancies(discrepancies: TaskProductDiscrepancies): Boolean
    fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun getCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProduct(product: TaskProductInfo): Double
    fun clear()
}