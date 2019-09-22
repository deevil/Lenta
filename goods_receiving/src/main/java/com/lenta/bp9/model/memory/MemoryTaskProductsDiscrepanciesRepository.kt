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

    override fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        var index = -1
        for (i in productsDiscrepancies.indices) {
            if (discrepancy.materialNumber == productsDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == productsDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            productsDiscrepancies.add(discrepancy)
            return true
        }
        return false
    }

    override fun changeProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        deleteProductDiscrepancy(discrepancy)
        return addProductDiscrepancy(discrepancy)
    }

    override fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        var index = -1
        for (i in productsDiscrepancies.indices) {
            if (discrepancy.materialNumber == productsDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == productsDiscrepancies[i].typeDiscrepancies) {
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
            it.typeDiscrepancies == "1"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies.toDouble()
        }
        return countAccept
    }

    override fun getCountRefusalOfProduct(product: TaskProductInfo): Double {
        var countRefusal = 0.0
        findProductDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies != "1"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }

    override fun getCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double {
        var countRefusal = 0.0
        reasonRejectionCode?.let {
            findProductDiscrepanciesOfProduct(product).filter {
                it.typeDiscrepancies == reasonRejectionCode
            }.map {discrepancies ->
                countRefusal += discrepancies.numberDiscrepancies.toDouble()
            }
        }
        return countRefusal
    }


    override fun clear() {
        productsDiscrepancies.clear()
    }
}