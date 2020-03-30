package com.lenta.bp12.model.pojo

import com.lenta.shared.fmp.resources.dao_ext.ReturnReason
import com.lenta.shared.fmp.resources.dao_ext.TaskType
import com.lenta.shared.utilities.extentions.sumWith

data class TaskCreate(
        val number: String = "",
        val name: String,
        val type: TaskType,
        val storage: String,
        val reason: ReturnReason,
        val isAlcohol: Boolean,
        val isCommon: Boolean,
        val goods: MutableList<Good> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),
        var isFinish: Boolean = false
) {

    fun getQuantityByBasket(basket: Basket?): Double {
        var quantity = 0.0

        goods.filter {
            it.section == basket?.section && it.type == basket.type && it.control == basket.control
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
            it.section == basket.section && it.type == basket.type && it.control == basket.control
        }.forEach { good ->
            val positionList = good.positions.filter {
                it.provider?.code == basket.provider?.code
            }

            good.deletePositions(positionList)
        }
    }

}