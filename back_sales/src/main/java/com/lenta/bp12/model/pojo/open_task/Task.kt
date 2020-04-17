package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.extentions.sumWith

data class Task(
        val number: String = "",
        val name: String,
        val type: TaskType?,
        val storage: String,
        val reason: ReturnReason,
        val goods: MutableList<Good> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),
        var isProcessed: Boolean = false,
        val isStrict: Boolean = false,
        var status: TaskStatus = TaskStatus.COMMON,
        val blockType: BlockType = BlockType.UNLOCK,
        val blockUser: String = "",
        val blockIp: String = "",
        val control: ControlType = ControlType.UNKNOWN,
        var comment: String = "",
        val provider: ProviderInfo? = null,
        val quantity: Int = 0
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

    fun getProviderCodeWithName(): String {
        return "${provider?.code} ${provider?.name}"
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