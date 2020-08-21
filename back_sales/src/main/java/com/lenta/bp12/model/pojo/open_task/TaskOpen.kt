package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Block
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.request.pojo.ProviderInfo

data class TaskOpen(
        val number: String,
        val name: String,
        val type: TaskType?,
        val block: Block,

        val storage: String,
        val control: ControlType,
        val provider: ProviderInfo,
        val reason: ReturnReason?,
        var comment: String,
        var section: String,
        val goodType: String,
        var purchaseGroup: String,
        var goodGroup: String,

        val numberOfGoods: Int,
        val goods: MutableList<GoodOpen> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf(),

        val isStrict: Boolean,
        var isFinished: Boolean
) {

    fun getProviderCodeWithName(): String {
        with(provider){
            return if (code.orEmpty().isNotEmpty() || name.orEmpty().isNotEmpty()) {
                "${code.orEmpty().dropWhile { it == '0' }} $name"
            } else ""
        }
    }

    fun isExistProcessedGood(): Boolean {
        return goods.any { it.isCounted || it.isDeleted }
    }

    fun isExistUncountedGood(): Boolean {
        return goods.any { !it.isCounted && !it.isDeleted }
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

    fun removeEmptyGoods() {
        goods.removeAll(goods.filter { it.getTotalQuantity() == 0.0 })
    }

    fun removeEmptyBaskets() {
        baskets.removeAll(baskets.filter { it.getGoodList().isEmpty() })
    }

    fun updateBasket(basket: Basket) {
        val oldBasketIndex = baskets.indexOfFirst { it.index == basket.index }
        baskets[oldBasketIndex] = basket
    }

    fun removeBaskets(basketList: MutableList<Basket>) {
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
                    val positionThatFits = good.positions.firstOrNull { positionFromTask ->
                        goodToDeleteFromBasket.positions.any { it.quantity >= positionFromTask.quantity}
                    }

                    positionThatFits?.let {
                        //Получим количество позиций этого товара
                        val quantityOfPositionFromTask = it.quantity
                        //Получим количество удаляемого товара из корзины
                        val quantityToMinus = basket.goods[goodToDeleteFromBasket] ?: 0.0
                        //Отнимем первое от второго и вернем в товар
                        val newQuantity = quantityOfPositionFromTask.minus(quantityToMinus)
                        it.quantity = newQuantity
                        val index = good.positions.indexOf(it)
                        good.positions.set(index, it)
                    }
                }
                baskets.remove(basket)
            }
            removeEmptyGoods()
        }
    }

}