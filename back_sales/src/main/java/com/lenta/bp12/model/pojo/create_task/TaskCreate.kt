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
        }

        removeEmptyBaskets()
    }

    fun removeGoodByBasketAndMaterials(basket: Basket, materials: MutableList<String>) {
        materials.forEach { material ->
            goods.find { it.material == material }?.let { good ->
                basket.provider?.let { provider ->
                    good.removeByProvider(provider.code)
                    if (good.isEmpty()) {
                        goods.remove(good)
                    }
                }
            }
        }

        removeEmptyGoods()
        removeEmptyBaskets()
    }

    fun updateBasket(basket: Basket){
        val oldBasketIndex = baskets.indexOfFirst { it.index == basket.index }
        baskets[oldBasketIndex] = basket
    }

    fun removeBaskets(basketList: MutableList<Basket>) {
        basketList.forEach { basket ->
            getGoodListByBasket(basket).forEach { good ->
                basket.provider?.let{ provider ->
                    good.removePositionsByProvider(provider.code)
                    good.removeMarksByProvider(provider.code)
                    good.removePartsByProvider(provider.code)
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