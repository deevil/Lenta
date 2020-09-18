package com.lenta.bp12.managers.base

import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.ZERO_QUANTITY
import kotlin.math.absoluteValue

abstract class BaseTaskManager : ITaskManager {

    protected open fun deleteGoodFromBaskets(task: Taskable, good: Good, count: Double) {
        var leftToDel = count.absoluteValue
        val baskets = task.getBasketsByGood(good).toMutableList()
        while (leftToDel > 0) {
            val lastBasket = baskets.lastOrNull()
            lastBasket?.let {
                val oldQuantity = lastBasket.goods[good]
                oldQuantity?.let {
                    val newQuantity = oldQuantity.minus(leftToDel)
                    if (newQuantity <= 0) {
                        leftToDel = newQuantity.absoluteValue
                        lastBasket.goods.remove(good)
                        baskets.remove(lastBasket)
                    } else {
                        lastBasket.goods[good] = newQuantity
                        leftToDel = ZERO_QUANTITY
                    }
                }
                updateCurrentBasket(it)
            }
        }
        task.removeEmptyBaskets()
        task.removeEmptyGoods()
    }

}