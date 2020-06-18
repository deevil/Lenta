package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Properties
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.shared.utilities.extentions.sumWith

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

    fun getQuantityByBasket(basket: Basket?): Double {
        var quantity = 0.0

        goods.filter {
            it.section == basket?.section && it.matype == basket.matype && it.control == basket.control
        }.forEach { good ->
            val positionQuantity = good.positions.filter {
                it.provider?.code == basket?.provider?.code
            }.map { it.quantity }.sum()

            quantity = quantity.sumWith(positionQuantity)
        }

        return quantity
    }

    fun deleteEmptyBaskets() {
        baskets.removeAll(baskets.filter { getQuantityByBasket(it) == 0.0 })
    }

    fun deleteGoodFromBasket(basket: Basket) {
        goods.filter {
            it.section == basket.section && it.matype == basket.matype && it.control == basket.control
        }.forEach { good ->
            val positionList = good.positions.filter {
                it.provider?.code == basket.provider?.code
            }

            good.deletePositions(positionList)
        }
    }

    fun updateGood(good: Good?) {
        good?.let { goodUpdate ->
            val index = goods.indexOf(goods.find { it.material == goodUpdate.material })
            if (index >= 0) {
                goods.removeAt(index)
            }

            goods.add(0, goodUpdate)
        }
    }

}