package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskProductRepository
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.extentions.addItemToListWithPredicate

class MemoryTaskProductRepository : ITaskProductRepository {

    private val productInfo: ArrayList<TaskProductInfo> = ArrayList()
    private val processingUnitsOfProduct: ArrayList<TaskProductInfo> = ArrayList()

    override fun getProducts(): List<TaskProductInfo> {
        return productInfo
    }

    override fun findProduct(product: TaskProductInfo): TaskProductInfo? {
        return findProduct(product.materialNumber)
    }

    override fun findProduct(materialNumber: String): TaskProductInfo? {
        return productInfo.firstOrNull { it.materialNumber == materialNumber}
    }

    override fun addProduct(product: TaskProductInfo): Boolean {
        return productInfo.addItemToListWithPredicate(product) {it.materialNumber == product.materialNumber}
    }

    private fun addProcessingUnitsOfProduct(processingUnit: TaskProductInfo): Boolean {
        var index = -1
        for (i in processingUnitsOfProduct.indices) {
            if (processingUnit.materialNumber == processingUnitsOfProduct[i].materialNumber && processingUnit.processingUnit == processingUnitsOfProduct[i].processingUnit) {
                index = i
            }
        }

        if (index == -1) {
            processingUnitsOfProduct.add(processingUnit)
            return true
        } else if (index !=  processingUnitsOfProduct.size - 1) {
            processingUnitsOfProduct.getOrNull(index)?.let {
                processingUnitsOfProduct.removeAt(index)
                processingUnitsOfProduct.add(it)
            }
        }

        return false
    }

    override fun updateProducts(newProducts: List<TaskProductInfo>) {
        clear()
        newProducts.map {
            addProduct(it)
            addProcessingUnitsOfProduct(it)
        }
    }

    override fun changeProduct(product: TaskProductInfo): Boolean {
        deleteProduct(product)
        return addProduct(product)
    }

    override fun deleteProduct(delProduct: TaskProductInfo): Boolean {
        productInfo.map { it }.filter {product ->
            if (delProduct.materialNumber == product.materialNumber) {
                productInfo.remove(product)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun getProcessingUnitsOfProduct(materialNumber: String): List<TaskProductInfo> {
        return processingUnitsOfProduct.filter {unitInfo ->
            unitInfo.materialNumber == materialNumber
        }
    }

    override fun clear() {
        productInfo.clear()
        processingUnitsOfProduct.clear()
    }
}