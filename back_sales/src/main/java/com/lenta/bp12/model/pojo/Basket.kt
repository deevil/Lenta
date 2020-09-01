package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.MarkType
import com.lenta.bp12.request.pojo.ProviderInfo

data class Basket(
        val index: Int,
        val section: String?,
        val goodType: String?,
        val control: ControlType?,
        val provider: ProviderInfo?,
        val volume: Double = 0.0,
        val goods: MutableMap<Good, Double> = mutableMapOf(),
        val markType: MarkType
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
}