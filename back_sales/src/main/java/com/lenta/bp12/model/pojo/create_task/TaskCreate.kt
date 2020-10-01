package com.lenta.bp12.model.pojo.create_task

import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.model.pojo.extentions.*

data class TaskCreate(
        override val name: String,
        val storage: String,
        val reason: ReturnReason?,
        override val type: TaskType,
        var isProcessed: Boolean = false,

        override val goods: MutableList<Good> = mutableListOf(),
        override val baskets: MutableList<Basket> = mutableListOf()
) : Taskable {

    override fun getFormattedName(withFullName: Boolean): String {
        return "${type.code} // $name"
    }

    override fun getBasketsByGood(good: Good): List<Basket> {
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
}