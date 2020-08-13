package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith

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

    fun getQuantityByBasket(basket: Basket): Double {
        return getGoodListByBasket(basket).map { good ->
            val positionQuantity = good.positions.filter { it.provider.code == basket.provider?.code }.map { it.quantity }.sumList()
            val markQuantity = good.marks.filter { it.providerCode == basket.provider?.code }.size.toDouble()
            val partQuantity = good.parts.filter { it.providerCode == basket.provider?.code }.map { it.quantity }.sumList()

            positionQuantity.sumWith(markQuantity).sumWith(partQuantity)
        }.sumList()
    }

    fun getGoodListByBasket(basket: Basket): List<GoodCreate> {
        val list = goods.filter { good ->
            good.section == basket.section && good.type == basket.goodType && good.control == basket.control &&
                    (good.positions.find { it.provider == basket.provider } != null ||
                            good.marks.find { it.providerCode == basket.provider?.code } != null ||
                            good.parts.find { it.providerCode == basket.provider?.code } != null)
        }
        return list
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