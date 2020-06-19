package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Properties
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.shared.utilities.extentions.sumList

data class Task(
        val number: String = "",
        val name: String,
        val properties: Properties,
        val storage: String,
        val reason: ReturnReason,
        val goods: MutableList<Good> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),
        var isProcessed: Boolean = false,
        val control: ControlType = ControlType.UNKNOWN,
        var comment: String = ""
) {

    fun getQuantityByBasket(basket: Basket): Double {
        return getGoodListByBasket(basket).map { good ->
            good.positions.filter { it.provider?.code == basket.provider?.code }.map { it.quantity }.sumList()
        }.sumList()
    }

    fun getGoodListByBasket(basket: Basket): List<Good> {
        return goods.filter { good ->
            good.section == basket.section && good.matype == basket.matype && good.control == basket.control &&
                    good.positions.find { it.provider == basket.provider } != null
        }
    }

    fun removeGoodByMaterials(materialList: List<String>) {
        materialList.forEach { material ->
            goods.remove(goods.find { it.material == material })
        }

        removeEmptyBaskets()
    }

    fun removeBaskets(basketList: MutableList<Basket>) {
        basketList.forEach { basket ->
            getGoodListByBasket(basket).forEach { good ->
                good.positions.filter { it.provider?.code == basket.provider?.code }.let { positions ->
                    good.removePositions(positions)
                }
            }

            baskets.remove(basket)
        }

        removeEmptyGoods()
    }

    private fun removeEmptyGoods() {
        goods.filter { it.getTotalQuantity() == 0.0 }.let { goodsForRemove ->
            goodsForRemove.forEach { good ->
                goods.remove(good)
            }
        }
    }

    private fun removeEmptyBaskets() {
        baskets.removeAll(baskets.filter { getQuantityByBasket(it) == 0.0 })
    }

}