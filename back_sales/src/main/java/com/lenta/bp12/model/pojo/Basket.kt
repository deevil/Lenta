package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.request.pojo.ProviderInfo

/** Корзина. Лежит в задании списке корзин задания (task.baskets)
 * Содержит в себе товары в вите мапы - <Товар - Количество>
 * Внимание! В программе товар лежит тот же самый что и в задании (task.goods)
 * в обоих классах ссылка на один и тот же инстанс в куче. Поэтому проводя изменения в товаре
 * в корзине вы меняете его и в общем списке товаров задания (не нужно делать одно и тоже два раза)
 * Методы:
 * @see com.lenta.bp12.model.pojo.extentions.addGood
 *
 * */
data class Basket(
        val index: Int,
        val section: String?,
        val goodType: String?,
        val control: ControlType?,
        val provider: ProviderInfo?,
        val volume: Double = 0.0,
        val goods: MutableMap<Good, Double> = mutableMapOf(),
        val markTypeGroup: MarkTypeGroup?,
        val purchaseGroup: String?,
        val mprGroup: String?
) {
    /**
     * МРЦ
     * */
    var maxRetailPrice: String = ""
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

    override fun toString(): String {
        return "Basket(index=$index, section=$section, goodType=$goodType, control=$control, provider=$provider, volume=$volume, goods=$goods, markTypeGroup=$markTypeGroup, purchaseGroup=$purchaseGroup, mprGroup=$mprGroup, maxRetailPrice='$maxRetailPrice', isPrinted=$isPrinted, isLocked=$isLocked, markedForLock=$markedForLock, freeVolume=$freeVolume)"
    }
}