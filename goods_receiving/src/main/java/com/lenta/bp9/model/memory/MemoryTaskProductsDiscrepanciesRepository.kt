package com.lenta.bp9.model.memory

import com.lenta.bp9.model.ReceivingProductDiscrepancies
import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.bp9.model.repositories.ITaskProductsDiscrepancies

class MemoryTaskProductsDiscrepanciesRepository : ITaskProductsDiscrepancies {

    private val productsDiscrepancies: ArrayList<ReceivingProductDiscrepancies> = ArrayList()

    override fun getProductsDiscrepancies(): List<ReceivingProductDiscrepancies> {
        return productsDiscrepancies
    }

    override fun findProductDiscrepanciesOfProduct(product: ReceivingProductInfo): List<ReceivingProductDiscrepancies> {
        val foundDiscrepancies = ArrayList<ReceivingProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (product.materialNumber == productsDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(productsDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addProductDiscrepancies(discrepancies: ReceivingProductDiscrepancies): Boolean {
        var index = -1
        for (i in productsDiscrepancies.indices) {
            if (discrepancies.materialNumber == productsDiscrepancies[i].materialNumber && discrepancies.typeDifferences == productsDiscrepancies[i].typeDifferences) {
                index = i
            }
        }

        if (index == -1) {
            productsDiscrepancies.add(discrepancies)
            return true
        }
        return false
    }

    override fun deleteProductsDiscrepancies(discrepancies: ReceivingProductDiscrepancies): Boolean {
        var index = -1
        for (i in productsDiscrepancies.indices) {
            if (discrepancies.materialNumber == productsDiscrepancies[i].materialNumber && discrepancies.typeDifferences == productsDiscrepancies[i].typeDifferences) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        productsDiscrepancies.removeAt(index)
        return true
    }

    override fun deleteProductDiscrepanciesForProduct(product: ReceivingProductInfo): Boolean {
        val delDiscrepancies = ArrayList<ReceivingProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (product.materialNumber == productsDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(productsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        productsDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun clear() {
        productsDiscrepancies.clear()
    }
}