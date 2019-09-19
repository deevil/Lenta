package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.ReceivingProductDiscrepancies
import com.lenta.bp9.model.ReceivingProductInfo

interface ITaskProductsDiscrepancies {
    fun getProductsDiscrepancies(): List<ReceivingProductDiscrepancies>
    fun findProductDiscrepanciesOfProduct(product: ReceivingProductInfo): List<ReceivingProductDiscrepancies>
    fun addProductDiscrepancies(discrepancies: ReceivingProductDiscrepancies): Boolean
    fun deleteProductsDiscrepancies(discrepancies: ReceivingProductDiscrepancies): Boolean
    fun deleteProductDiscrepanciesForProduct(product: ReceivingProductInfo): Boolean
    fun clear()
}