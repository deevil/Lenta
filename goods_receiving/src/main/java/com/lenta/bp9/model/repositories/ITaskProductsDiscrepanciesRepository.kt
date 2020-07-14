package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskProductsDiscrepanciesRepository {
    fun getProductsDiscrepancies(): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(materialNumber: String): List<TaskProductDiscrepancies>
    fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun updateProductsDiscrepancy(newProductsDiscrepancies: List<TaskProductDiscrepancies>)
    fun changeProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancy(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesForProduct(materialNumber: String): Boolean
    fun deleteProductsDiscrepanciesNotNormForProduct(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun changeProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancyNotRecountPGE(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteProductsDiscrepanciesForProductNotRecountPGE(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesOfProductOfDiscrepanciesNotRecountPGE(product: TaskProductInfo, typeDiscrepancies: String): Boolean
    fun getCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProduct(product: TaskProductInfo): Double
    fun getCountAcceptOfProductPGE(product: TaskProductInfo): Double
    fun getCountRefusalOfProductPGE(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProductPGE(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProductPGEOfProcessingUnits(product: TaskProductInfo, orderQuantity: Double) : Double
    fun getCountOfDiscrepanciesOfProduct(product: TaskProductInfo, typeDiscrepancies: String): Double
    fun getCountOfDiscrepanciesOfProduct(materialNumber: String, typeDiscrepancies: String): Double
    fun getQuantityDiscrepanciesOfProduct(product: TaskProductInfo): Int
    fun getAllCountDiscrepanciesOfProduct(product: TaskProductInfo): Double
    fun getAllCountDiscrepanciesOfProduct(materialNumber: String): Double
    fun clear()
}