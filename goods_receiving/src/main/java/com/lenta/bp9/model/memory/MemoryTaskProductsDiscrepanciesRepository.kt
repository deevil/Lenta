package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskProductsDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.extentions.addItemToListWithPredicate
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskProductsDiscrepanciesRepository : ITaskProductsDiscrepanciesRepository {

    private val productsDiscrepancies: ArrayList<TaskProductDiscrepancies> = ArrayList()

    override fun getProductsDiscrepancies(): List<TaskProductDiscrepancies> {
        return productsDiscrepancies
    }

    override fun findProductDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskProductDiscrepancies> {
        return findProductDiscrepanciesOfProduct(product.materialNumber)
    }

    override fun findProductDiscrepanciesOfProduct(materialNumber: String): List<TaskProductDiscrepancies> {
        return productsDiscrepancies.filter { it.materialNumber == materialNumber }
    }

    override fun findProductDiscrepanciesOfProductOfProcessingUnit(product: TaskProductInfo): List<TaskProductDiscrepancies> {
        return findProductDiscrepanciesOfProductOfProcessingUnit(product.materialNumber, product.processingUnit)
    }

    override fun findProductDiscrepanciesOfProductOfProcessingUnit(materialNumber: String, processingUnitNumber: String): List<TaskProductDiscrepancies> {
        return productsDiscrepancies
                .filter { it.materialNumber == materialNumber
                        && it.processingUnitNumber == processingUnitNumber
                }
    }

    override fun addProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        return productsDiscrepancies.addItemToListWithPredicate(discrepancy) {
            it.materialNumber == discrepancy.materialNumber
                    && it.typeDiscrepancies == discrepancy.typeDiscrepancies
                    && it.processingUnitNumber == discrepancy.processingUnitNumber
        }
    }

    override fun addProductDiscrepancyOfMercuryDiscrepancy(mercuryDiscrepancies: List<TaskMercuryDiscrepancies>) {
        mercuryDiscrepancies
                .filter { it.typeDiscrepancies.isNotEmpty() }
                .groupBy { it.materialNumber }
                .forEach { groupByMaterialNumberMercuryDiscrepancies ->
                    groupByMaterialNumberMercuryDiscrepancies.value
                            .groupBy { it.typeDiscrepancies }
                            .forEach { groupByMercuryDiscrepancies ->
                                val countDiscrepancies =
                                        groupByMercuryDiscrepancies.value
                                                .map { it.numberDiscrepancies }
                                                .sumByDouble { it }

                                groupByMercuryDiscrepancies.value
                                        .first()
                                        .let {
                                            val productDiscrepancies = TaskProductDiscrepancies.fromMercury(it.copy(numberDiscrepancies = countDiscrepancies))
                                            addProductDiscrepancy(productDiscrepancies)
                                        }
                            }
                }
    }

    override fun updateProductsDiscrepancy(newProductsDiscrepancies: List<TaskProductDiscrepancies>) {
        clear()
        newProductsDiscrepancies.map {
            addProductDiscrepancy(it)
        }
    }

    override fun changeProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        deleteProductDiscrepancy(discrepancy)
        return addProductDiscrepancy(discrepancy)
    }

    override fun deleteProductDiscrepancy(discrepancy: TaskProductDiscrepancies): Boolean {
        return deleteProductDiscrepancy(discrepancy.materialNumber, discrepancy.typeDiscrepancies)
    }

    override fun deleteProductDiscrepancy(materialNumber: String, typeDiscrepancies: String): Boolean {
        return productsDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.typeDiscrepancies == typeDiscrepancies
                    && !it.isNotEdit
        }
    }

    override fun changeProductDiscrepancyOfProcessingUnit(discrepancy: TaskProductDiscrepancies): Boolean {
        deleteProductDiscrepancyOfProcessingUnit(discrepancy.materialNumber, discrepancy.typeDiscrepancies, discrepancy.processingUnitNumber)
        return addProductDiscrepancy(discrepancy)
    }

    override fun deleteProductDiscrepancyOfProcessingUnit(materialNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Boolean {
        return productsDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.processingUnitNumber == processingUnitNumber
                    && it.typeDiscrepancies == typeDiscrepancies
                    && !it.isNotEdit
        }
    }

    override fun deleteProductsDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        return deleteProductsDiscrepanciesForProduct(product.materialNumber)
    }

    override fun deleteProductsDiscrepanciesForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (materialNumber == productsDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(productsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteProductDiscrepancy(it)
        }
        return true
    }

    override fun deleteProductsDiscrepanciesNotNormForProduct(product: TaskProductInfo): Boolean {
        return deleteProductsDiscrepanciesNotNormForProduct(product.materialNumber)
    }

    override fun deleteProductsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (materialNumber == productsDiscrepancies[i].materialNumber && productsDiscrepancies[i].typeDiscrepancies != "1") {
                delDiscrepancies.add(productsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteProductDiscrepancy(it)
        }
        return true
    }

    override fun changeProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean {
        deleteProductDiscrepancyNotRecountPGE(discrepancy)
        return addProductDiscrepancy(discrepancy)
    }

    override fun deleteProductDiscrepancyNotRecountPGE(discrepancy: TaskProductDiscrepancies): Boolean {
        return deleteProductDiscrepancyNotRecountPGE(discrepancy.materialNumber, discrepancy.typeDiscrepancies)
    }

    override fun deleteProductDiscrepancyNotRecountPGE(materialNumber: String, typeDiscrepancies: String): Boolean {
        productsDiscrepancies.map { it }.filter {discrepancies ->
            if (materialNumber == discrepancies.materialNumber && typeDiscrepancies == discrepancies.typeDiscrepancies) {
                productsDiscrepancies.remove(discrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteProductsDiscrepanciesForProductNotRecountPGE(product: TaskProductInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskProductDiscrepancies>()
        for (i in productsDiscrepancies.indices) {
            if (product.materialNumber == productsDiscrepancies[i].materialNumber && productsDiscrepancies[i].typeDiscrepancies != "1") {
                delDiscrepancies.add(productsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteProductDiscrepancyNotRecountPGE(it)
            findProductDiscrepanciesOfProduct(product).findLast {findNormDiscrepancies ->
                findNormDiscrepancies.typeDiscrepancies == "1"
            }?.let {normDiscrepancies ->
                changeProductDiscrepancyNotRecountPGE(normDiscrepancies.copy(numberDiscrepancies = normDiscrepancies.notEditNumberDiscrepancies))
            }
        }
        return true
    }

    override fun deleteProductsDiscrepanciesOfProductOfDiscrepanciesNotRecountPGE(product: TaskProductInfo, typeDiscrepancies: String): Boolean {
        productsDiscrepancies.map { it }.filter {discrepancies ->
            if (product.materialNumber == discrepancies.materialNumber && typeDiscrepancies == discrepancies.typeDiscrepancies && discrepancies.typeDiscrepancies != "1") {
                productsDiscrepancies.remove(discrepancies)
                findProductDiscrepanciesOfProduct(product).findLast {findNormDiscrepancies ->
                    findNormDiscrepancies.typeDiscrepancies == "1"
                }?.let {normDiscrepancies ->
                    changeProductDiscrepancyNotRecountPGE(normDiscrepancies.copy(numberDiscrepancies = (normDiscrepancies.numberDiscrepancies.toDouble() + discrepancies.numberDiscrepancies.toDouble()).toString()))
                }
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteProductDiscrepancyByBatch(materialNumber: String, typeDiscrepancies: String, quantityByDiscrepancyForBatch: Double) {
        val quantityByDiscrepancyForProduct =
                productsDiscrepancies
                        .findLast {
                            it.materialNumber == materialNumber
                                    && it.typeDiscrepancies == typeDiscrepancies
                        }
                        ?.numberDiscrepancies
                        ?.toDouble()
                        ?: 0.0

        val residueByDiscrepancyForProduct =
                quantityByDiscrepancyForProduct
                        .takeIf { it > 0.0 }
                        ?.let { it - quantityByDiscrepancyForBatch }
                        ?: 0.0

        val productDiscrepanciesOfProduct = findProductDiscrepanciesOfProduct(materialNumber).findLast { it.typeDiscrepancies == typeDiscrepancies }
        if (residueByDiscrepancyForProduct > 0.0) {
            productDiscrepanciesOfProduct?.let { changeProductDiscrepancy(it.copy(numberDiscrepancies = residueByDiscrepancyForProduct.toString())) }
        } else {
            productDiscrepanciesOfProduct?.let { deleteProductDiscrepancy(it) }
        }
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

    override fun getCountProductNotProcessedOfProduct(product: TaskProductInfo) : Double {
        return String.format("%.3f", product.origQuantity.toDouble() - getCountAcceptOfProduct(product) - getCountRefusalOfProduct(product)).toDoubleOrNull() ?: 0.0
    }

    override fun getCountAcceptOfProductPGE(product: TaskProductInfo): Double {
        var countAccept = 0.0
        findProductDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies == "1" || it.typeDiscrepancies == "2"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies.toDouble()
        }
        return countAccept
    }

    override fun getCountRefusalOfProductPGE(product: TaskProductInfo): Double {
        var countRefusal = 0.0
        findProductDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies == "3" || it.typeDiscrepancies == "4" || it.typeDiscrepancies == "5"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }

    override fun getCountProductNotProcessedOfProductPGE(product: TaskProductInfo) : Double {
        return product.orderQuantity.toDouble() - getCountAcceptOfProductPGE(product) - getCountRefusalOfProductPGE(product)
    }

    override fun getCountProductNotProcessedOfProductPGEOfProcessingUnits(product: TaskProductInfo, orderQuantity: Double) : Double {
        return orderQuantity - getCountAcceptOfProductPGE(product) - getCountRefusalOfProductPGE(product)
    }

    override fun getCountOfDiscrepanciesOfProductOfProcessingUnit(product: TaskProductInfo, typeDiscrepancies: String, processingUnitNumber: String): Double {
        var countDiscrepancies = 0.0
        findProductDiscrepanciesOfProductOfProcessingUnit(product.materialNumber, processingUnitNumber).filter {
            it.typeDiscrepancies == typeDiscrepancies
        }.map {discrepancies ->
            countDiscrepancies += discrepancies.numberDiscrepancies.toDouble()
        }
        return countDiscrepancies
    }

    override fun getCountOfDiscrepanciesOfProductOfProcessingUnit(materialNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Double {
        var countDiscrepancies = 0.0
        findProductDiscrepanciesOfProductOfProcessingUnit(materialNumber, processingUnitNumber).filter {
            it.typeDiscrepancies == typeDiscrepancies
        }.map {discrepancies ->
            countDiscrepancies += discrepancies.numberDiscrepancies.toDouble()
        }
        return countDiscrepancies
    }

    override fun getQuantityDiscrepanciesOfProduct(product: TaskProductInfo): Int {
        return findProductDiscrepanciesOfProduct(product).size
    }

    override fun getAllCountDiscrepanciesOfProduct(product: TaskProductInfo): Double {
        return findProductDiscrepanciesOfProduct(product).sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    override fun getAllCountDiscrepanciesOfProduct(materialNumber: String): Double {
        return findProductDiscrepanciesOfProduct(materialNumber).sumByDouble {
            it.numberDiscrepancies.toDouble()
        }
    }

    override fun clear() {
        productsDiscrepancies.clear()
    }
}