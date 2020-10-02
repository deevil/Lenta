package com.lenta.bp12.model

import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.TaskType

/**
 * @see com.lenta.bp12.model.pojo.create_task.TaskCreate
 * @see com.lenta.bp12.model.pojo.open_task.TaskOpen
 * */
interface Taskable{

    val name: String
    val baskets: MutableList<Basket>
    val goods: MutableList<Good>
    val type: TaskType?

    fun updateBasket(basket: Basket)
    fun removeBaskets(basketList: MutableList<Basket>)
    fun getBasketsByGood(good: Good): List<Basket>
    fun removeEmptyGoods()
    fun removeEmptyBaskets()
    fun getFormattedName(withFullName: Boolean = false): String
}