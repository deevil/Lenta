package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.orIfNull

data class Basket(
        val index: Int,
        val section: String?,
        val goodType: String?,
        val control: ControlType?,
        val provider: ProviderInfo?,
        val volume: Double = 0.0,
        val goods: MutableMap<Good, Double> = mutableMapOf()
) {
    /**
     * Распечатана ли
     */
    var isPrinted: Boolean = false

    /**
     * Закрыта ли
     */
    var isLocked: Boolean = false

    /**
     * Флаг для того чтобы когда закончится цикл добавления товаров в корзину, все эти корзины сделать закрытыми
     */
    var markedForLock: Boolean = false

    /**
     * Оставшийся объем после добавления товара
     */
    var freeVolume: Double = volume

    /**
     * */
    fun addGood(good: Good, quantity: Double) {
        if (freeVolume >= good.volume * quantity) {
            freeVolume -= (good.volume * quantity)
            val oldQuantity = goods[good].orIfNull { 0.0 }
            val newQuantity = quantity + oldQuantity
            goods[good] = newQuantity
        }
    }

    fun deleteGood(good: Good) {
        if (freeVolume + good.volume <= volume) {
            val oldQuantity = goods[good].orIfNull { 0.0 }
            val volumeToReturnToBasket = oldQuantity * good.volume
            freeVolume += volumeToReturnToBasket
            goods.remove(good)
        }
    }

    fun getDescription(isDivBySection: Boolean): String {

        return buildString {
            val sectionBlock = if (isDivBySection) "C-$section/" else ""
            append(sectionBlock)

            val goodTypeBlock = if (goodType.isNullOrEmpty()) "" else "$goodType/"
            append(goodTypeBlock)

            append("${control?.code}")

            val providerBlock = if (provider?.code.isNullOrEmpty()) "" else "/ПП-${provider?.code}"

            append(providerBlock)
        }
    }

    fun getGoodList() : List<Good> {
        return goods.keys.toList()
    }

    fun getQuantityOfGood(good: Good): Double {
        return goods[good] ?: 0.0
    }

    fun getQuantityFromGoodList() : Int {
        return getGoodList().size
    }
}