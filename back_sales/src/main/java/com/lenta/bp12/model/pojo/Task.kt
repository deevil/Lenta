package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.TaskStatus
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.utilities.extentions.sumWith

data class Task(
        val number: String = "",
        val name: String,
        val type: TaskType?,
        val storage: String,
        val reason: ReturnReason,
        //val isAlcoholAllowed: Boolean = false,
        //val isCommonAllowed: Boolean = false,
        val goods: MutableList<Good> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),
        var isFinish: Boolean = false,

        val isStrict: Boolean = false,
        var status: TaskStatus = TaskStatus.COMMON,
        val blockType: BlockType = BlockType.UNLOCK,
        val blockUser: String = "",
        val blockIp: String = "",
        val control: ControlType = ControlType.UNKNOWN,
        var comment: String = "",
        val provider: ProviderInfo? = null,
        val quantity: Int = 0
        //val section: String = "",
        //val purchaseGroup: String = "",
        //val goodGroup: String = ""
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

}