package com.lenta.movement.models

import com.lenta.movement.R
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
    val isFood: Boolean?,
    val isMarked: Boolean?,
    val matkl: String?,
    val materialType: String?,
    val sectionId: String?
) : MutableMap<ProductInfo, Int> by mutableMapOf() {

    override val size: Int
        get() = values.sum()

    val number: Int
        get() = index + 1

    private val freeVolume: Double
        get() = volume - map { (product, count) -> product.volume * count }.sum()

    fun getByIndex(index: Int): ProductInfo? {
        return keys.toList().getOrNull(index)
    }

    fun checkSuitableProduct(product: ProductInfo, supplier: Supplier?): Boolean {
        if (isEmpty()) return true

        if (product.volume > freeVolume) return false

        return isAlco?.equals(product.isAlco) ?: true &&
                isExciseAlco?.equals(product.isExcise) ?: true &&
                isNotExciseAlco?.equals(product.isNotExcise) ?: true &&
                isUsual?.equals(product.isUsual) ?: true &&
                isVet?.equals(product.isVet) ?: true &&
                isFood?.equals(product.isFood) ?: true &&
                this.supplier?.equals(supplier) ?: true &&
                matkl?.equals(product.matkl) ?: true &&
                materialType?.equals(product.materialType) ?: true &&
                sectionId?.equals(product.sectionId) ?: true
    }

    /**
     * Получение сведений о корзине, является ли деление по данному признаку правильным
     */
    fun isDivisionTrue(division: GoodsSignOfDivision) = when(division) {
        GoodsSignOfDivision.MARK_PARTS -> isMarked != null && isMarked
        GoodsSignOfDivision.ALCO -> isAlco != null && isAlco
        GoodsSignOfDivision.USUAL -> isUsual != null && isUsual
        GoodsSignOfDivision.VET -> isVet != null && isVet
        GoodsSignOfDivision.FOOD -> isFood != null && isFood
        else -> false
    }

    fun getStringDescription(division: GoodsSignOfDivision): String = when(division) {
        GoodsSignOfDivision.SECTION -> "$sectionId"
        GoodsSignOfDivision.LIF_NUMBER -> "${supplier?.code} ${supplier?.name}"
        GoodsSignOfDivision.MATERIAL_NUMBER -> "$matkl"
        else -> ""
    }
}