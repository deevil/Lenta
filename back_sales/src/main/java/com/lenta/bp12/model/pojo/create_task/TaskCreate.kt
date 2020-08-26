package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.extentions.*

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

    fun getBasketsByGood(good: GoodCreate): List<Basket> {
        return baskets.filter { basket ->
            basket.getGoodList().any { it.material == good.material }
        }
    }

    fun getCountByBasket(basket: Basket): Int {
        return basket.getGoodList().size
    }

    fun removeGoodByMaterials(materials: List<String>) {
        materials.forEach { material ->
            goods.remove(goods.find { it.material == material })

            baskets.forEach { basket ->
                val goodList = basket.getGoodList().filter { it.material == material }
                goodList.forEach { good ->
                    basket.deleteGood(good)
                }
            }
        }
        removeEmptyBaskets()
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

    fun removeEmptyGoods() {
        goods.removeAll(goods.filter { it.getTotalQuantity() == 0.0 })
    }

    fun removeEmptyBaskets() {
        baskets.removeAll(baskets.filter { it.getGoodList().isEmpty() })
    }
}