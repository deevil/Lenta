package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskProductsDiscrepanciesRepository {
    fun getProductsDiscrepancies(): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies>
    fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun updateProductsDiscrepancy(newProductsDiscrepancies: List<TaskProductDiscrepancies>)
    fun changeProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun getCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double
    fun clear()
}