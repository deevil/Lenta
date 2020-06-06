package com.lenta.movement.models

import com.lenta.shared.models.core.Supplier

class Basket(
    val index: Int,
    val volume: Double,
    val supplier: Supplier?,
    val isAlco: Boolean?,
    val isExciseAlco: Boolean?,
    val isNotExciseAlco: Boolean?,
    val isUsual: Boolean?,
    val isVet: Boolean?,
    val isFood: Boolean?
) : MutableMap<ProductInfo, Int> by mutableMapOf() {

    override val size: Int
        get() = values.sum()

    val number: Int
        get() = index + 1

    val freeVolume: Double
        get() = volume - map { (product, count) -> product.volume * count }.sum()

    fun getByIndex(index: Int): ProductInfo {
        return keys.toList()[index]
    }

    fun checkSuitableProduct(product: ProductInfo, supplier: Supplier?): Boolean {
        if (isEmpty()) return true

        if (product.volume > freeVolume) return false

        return isAlco?.and(product.isAlco) ?: true &&
                isExciseAlco?.and(product.isExcise) ?: true &&
                isNotExciseAlco?.and(product.isNotExcise) ?: true &&
                isUsual?.and(product.isUsual) ?: true &&
                isVet?.and(product.isVet) ?: true &&
                isFood?.and(product.isFood) ?: true &&
                this.supplier?.equals(supplier) ?: true
    }
}