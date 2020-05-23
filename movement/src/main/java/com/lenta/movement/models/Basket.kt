package com.lenta.movement.models

import com.lenta.shared.models.core.Supplier

class Basket(
    val index: Int,
    val volume: Double,
    val supplier: Supplier?
) : MutableMap<ProductInfo, Int> by mutableMapOf() {

    override val size: Int
        get() = values.sum()

    val number: Int
        get() = index + 1

    val filledVolume: Double
        get() = map { (product, count) -> product.volume * count }.sum()

    val freeVolume: Double
        get() = volume - filledVolume

    fun getByIndex(index: Int): ProductInfo {
        return keys.toList()[index]
    }

}