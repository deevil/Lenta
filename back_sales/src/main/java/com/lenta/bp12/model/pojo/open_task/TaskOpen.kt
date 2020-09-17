package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.*
import com.lenta.bp12.model.pojo.extentions.*
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.taskContentNetRequest.Mrc

data class TaskOpen(
        val number: String,
        override val name: String,
        val type: TaskType?,
        val block: Block,

        val storage: String,
        override val control: ControlType,
        val provider: ProviderInfo,
        val reason: ReturnReason?,
        var comment: String,
        var section: String,
        val goodType: String,
        var purchaseGroup: String,
        var goodGroup: String,

        val numberOfGoods: Int,
        val goods: MutableList<Good> = mutableListOf(),
        override val baskets: MutableList<Basket> = mutableListOf(),
        val mrcList: MutableList<Mrc> = mutableListOf(),

        val isStrict: Boolean,
        var isFinished: Boolean
) : Taskable {

    fun getProviderCodeWithName(): String {
        with(provider) {
            return if (code.orEmpty().isNotEmpty() || name.orEmpty().isNotEmpty()) {
                "${code.orEmpty().dropWhile { it == '0' }} $name"
            } else ""
        }
    }

    fun isExistProcessedGood(): Boolean {
        return goods.any { it.isCounted || it.isDeleted }
    }

    fun isQuantityOfNotDeletedGoodsNotActual(): Boolean {
        return goods.any {
            it.isNotDeletedAndQuantityNotActual()
        }
    }

    fun getFormattedName(withFullName: Boolean = false): String {
        var formattedName = name
        if (!withFullName) {
            runCatching {
                formattedName = name.split(" ")[2]
            }
        }

        return "${type?.code}-$number // $formattedName"
    }


    override fun updateBasket(basket: Basket) {
        val oldBasketIndex = baskets.indexOfFirst { it.index == basket.index }
        baskets[oldBasketIndex] = basket
    }

    override fun removeBaskets(basketList: MutableList<Basket>) {
        //Пройдемся по всем корзинам что нужно удалить
        basketList.forEach { basket ->
            val basketIndex = basket.index
            //Получим список товаров корзины
            val goodsToDeleteFromBasket = basket.getGoodList()
            //Найдем их в общем списке
            goodsToDeleteFromBasket.forEach { goodToDeleteFromBasket ->
                val goodToDeleteFromTask = goods.firstOrNull { goodFromTask ->
                    goodToDeleteFromBasket.material == goodFromTask.material
                }
                goodToDeleteFromTask?.let { good ->
                    //Удалим у этого товара марки и партии с номером корзины
                    good.removeMarksByBasketIndex(basketIndex)
                    good.removePartsByBasketNumber(basketIndex)
                    good.removePositionsByBasketIndex(basketIndex)
                    //Найдем у этого товара позиции с подходящим количеством
                    good.deletePositionsFromTask(
                            goodFromBasket = goodToDeleteFromBasket,
                            basketToGetQuantity = basket
                    )
                }
                baskets.remove(basket)
            }
            removeEmptyGoods()
        }
    }

    override fun removeEmptyGoods() {
        goods.removeAll(goods.filter { it.getTotalQuantity() == 0.0 })
    }

    override fun removeEmptyBaskets() {
        baskets.removeAll(baskets.filter { it.getGoodList().isEmpty() })
    }

    override fun getBasketsByGood(good: Good): List<Basket> {
        return baskets.filter { basket ->
            basket.getGoodList().any { it.material == good.material }
        }
    }

    fun isMrcNotInTaskMrcList(formattedMrc: String) = this.mrcList.none { it.maxRetailPrice == formattedMrc }
}