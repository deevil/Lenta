package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType

data class TaskCreate(
        val name: String,
        val storage: String,
        val reason: ReturnReason,
        val type: TaskType,
        val control: ControlType = ControlType.UNKNOWN,
        var isProcessed: Boolean = false,

        val goods: MutableList<GoodCreate> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf()
) {

    fun getFormattedName(): String {
        return "${type.code} // $name"
    }

    fun getGoodListByBasket(basket: Basket): List<GoodCreate> {
        return basket.goods.keys.toList()
    }

    fun getBasketsByGood(good: GoodCreate): List<Basket> {
        return baskets.filter { basket ->
            basket.section == good.section && basket.goodType == good.type && basket.control == good.control &&
                    good.positions.find { it.provider == basket.provider } != null
        }
    }

    fun getCountByBasket(basket: Basket): Int {
        return getGoodListByBasket(basket).size
    }

    fun removeGoodByMaterials(materials: List<String>) {
        materials.forEach { material ->
            goods.remove(goods.find { it.material == material })

            baskets.forEach { basket ->
                basket.goods.keys.removeAll { it.material == material }
            }
        }

        removeEmptyBaskets()
    }

    fun updateBasket(basket: Basket) {
        val oldBasketIndex = baskets.indexOfFirst { it.index == basket.index }
        baskets[oldBasketIndex] = basket
    }

    //TODO NEEDS REFACTOR TOO MANY CYCLES BAD CODE
    fun removeBaskets(basketList: MutableList<Basket>) {
        basketList.forEach { basket ->
            goods.forEach { good ->
                val basketIndex = basket.index
                good.marks.removeAll { it.basketNumber == basketIndex }
                good.parts.removeAll { it.basketNumber == basketIndex }
                val basketGoodList = basket.goods.keys.toList()
                basketGoodList.forEach { goodFromBasket ->
                    val positionThatFits = good.positions.firstOrNull { it.quantity > 0 && goodFromBasket.material == it.materialNumber }
                    val quantity = positionThatFits?.quantity?:0.0
                    val quantityToMinus = basket.goods[goodFromBasket]?:0.0
                    val newQuantity = quantity.minus(quantityToMinus)
                    positionThatFits?.let {
                        it.quantity = newQuantity
                        val index = good.positions.indexOf(it)
                        good.positions.set(index, it)
                    }
                }
            }
            baskets.remove(basket)
        }
        removeEmptyGoods()
    }

    fun removeEmptyGoods() {
        goods.removeAll(goods.filter { it.getTotalQuantity() == 0.0 })
    }

    fun removeEmptyBaskets() {
        baskets.removeAll(baskets.filter { getGoodListByBasket(it).isEmpty() })
    }

    fun isExistBasket(basket: Basket): Boolean {
        return baskets.contains(basket)
    }

    fun getBasketNumber(good: GoodCreate, providerCode: String): String {
        val basket = baskets.find {
            it.section == good.section && it.goodType == good.type &&
                    it.control == good.control && it.provider?.code == providerCode
        }

        return "${baskets.indexOf(basket) + 1}"
    }

}