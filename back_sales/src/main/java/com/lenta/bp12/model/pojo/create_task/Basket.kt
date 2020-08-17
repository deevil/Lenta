package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.orIfNull

data class Basket(
        val index: Int,
        val section: String?,
        val goodType: String?,
        val control: ControlType?,
        val provider: ProviderInfo?,
        var quantity: String? = "",
        val volume: Double = 0.0,
        val goods: MutableMap<GoodCreate, Double> = mutableMapOf()
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

    fun addGood(good: GoodCreate, quantity: Double) {
        freeVolume -= (good.volume * quantity)
        val oldQuantity = goods[good].orIfNull { 0.0 }
        val newQuantity = quantity + oldQuantity
        goods[good] = newQuantity
    }
    //TODO
    fun deleteGood(good: GoodCreate) {
        if ((freeVolume + good.volume) <= volume) {
            freeVolume += good.volume
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

}