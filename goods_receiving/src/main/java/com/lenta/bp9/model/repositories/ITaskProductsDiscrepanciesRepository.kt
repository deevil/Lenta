package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.mobrun.plugin.api.HyperHive

interface ITaskProductsDiscrepanciesRepository {
    fun getProductsDiscrepancies(): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(materialNumber: String): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProductOfProcessingUnit(product: TaskProductInfo): List<TaskProductDiscrepancies>
    fun findProductDiscrepanciesOfProductOfProcessingUnit(materialNumber: String, processingUnitNumber: String): List<TaskProductDiscrepancies>
    fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun addProductDiscrepancyOfMercuryDiscrepancy(mercuryDiscrepancies: List<TaskMercuryDiscrepancies>)
    fun updateProductsDiscrepancy(newProductsDiscrepancies: List<TaskProductDiscrepancies>)
    fun changeProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancy(materialNumber: String, typeDiscrepancies: String): Boolean
    fun changeProductDiscrepancyOfProcessingUnit(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancyOfProcessingUnit(materialNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Boolean
    fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesForProduct(materialNumber: String): Boolean
    fun deleteProductsDiscrepanciesNotNormForProduct(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun changeProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean
    fun deleteProductDiscrepancyNotRecountPGE(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteProductsDiscrepanciesForProductNotRecountPGE(product: TaskProductInfo): Boolean
    fun deleteProductsDiscrepanciesOfProductOfDiscrepanciesNotRecountPGE(product: TaskProductInfo, typeDiscrepancies: String): Boolean
    fun deleteProductDiscrepancyByBatch(materialNumber: String, typeDiscrepancies: String, quantityByDiscrepancyForBatch: Double)
    fun getCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProduct(product: TaskProductInfo): Double
    fun getCountAcceptOfProductPGE(product: TaskProductInfo): Double
    fun getCountRefusalOfProductPGE(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProductPGE(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProductPGEOfProcessingUnits(product: TaskProductInfo, orderQuantity: Double) : Double
    fun getCountOfDiscrepanciesOfProductOfProcessingUnit(product: TaskProductInfo, typeDiscrepancies: String, processingUnitNumber: String): Double
    fun getCountOfDiscrepanciesOfProductOfProcessingUnit(materialNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Double
    fun getQuantityDiscrepanciesOfProduct(product: TaskProductInfo): Int
    fun getAllCountDiscrepanciesOfProduct(product: TaskProductInfo): Double
    fun getAllCountDiscrepanciesOfProduct(materialNumber: String): Double
    fun clear()
}