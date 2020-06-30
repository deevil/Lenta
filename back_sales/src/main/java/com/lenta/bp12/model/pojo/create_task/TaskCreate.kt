package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.shared.utilities.extentions.sumList
import com.lenta.shared.utilities.extentions.sumWith

data class TaskCreate(
        val name: String,
        val storage: String,
        val reason: ReturnReason,
        val taskType: TaskType,
        val control: ControlType = ControlType.UNKNOWN,
        var isProcessed: Boolean = false,

        val goods: MutableList<GoodCreate> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf()
) {

    fun getQuantityByBasket(basket: Basket): Double {
        return getGoodListByBasket(basket).map { good ->
            val positionQuantity = good.positions.filter { it.provider.code == basket.provider.code }.map { it.quantity }.sumList()
            val markQuantity = good.marks.filter { it.providerCode == basket.provider.code }.size.toDouble()
            val partQuantity = good.parts.filter { it.providerCode == basket.provider.code }.map { it.quantity }.sumList()

            positionQuantity.sumWith(markQuantity).sumWith(partQuantity)
        }.sumList()
    }

    fun getGoodListByBasket(basket: Basket): List<GoodCreate> {
        return goods.filter { good ->
            good.section == basket.section && good.matype == basket.matype && good.control == basket.control &&
                    (good.positions.find { it.provider == basket.provider } != null ||
                            good.marks.find { it.providerCode == basket.provider.code } != null ||
                            good.parts.find { it.providerCode == basket.provider.code } != null)
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
                good.removePositionsByProvider(basket.provider.code)
                good.removeMarksByProvider(basket.provider.code)
                good.removePartsByProvider(basket.provider.code)
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