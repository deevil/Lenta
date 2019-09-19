package com.lenta.bp9.model.memory

import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.repositories.ITaskProductsDiscrepanciesRepository

class MemoryTaskProductsDiscrepanciesRepository : ITaskProductsDiscrepanciesRepository {

    private val productsDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()

    override fun getProductsDiscrepancies(): List<TaskProductDiscrepancies> {
        return productsDiscrepancies
    }

    override fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (product.materialNumber == productsDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(productsDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addProductDiscrepancies(discrepancies: TaskProductDiscrepancies): Boolean {
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

    override fun deleteProductDiscrepancies(discrepancies: TaskProductDiscrepancies): Boolean {
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

    override fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskProductDiscrepancies>()
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

    override fun getCountAcceptOfProduct(product: TaskProductInfo): Double {
        var countAccept = 0.0
        findProductDiscrepanciesOfProduct(product).filter {
            it.typeDifferences == "1"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies.toDouble()
        }
        return countAccept
    }

    override fun getCountRefusalOfProduct(product: TaskProductInfo): Double {
        var countRefusal = 0.0
        findProductDiscrepanciesOfProduct(product).filter {
            it.typeDifferences != "1"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }


    override fun clear() {
        productsDiscrepancies.clear()
    }
}