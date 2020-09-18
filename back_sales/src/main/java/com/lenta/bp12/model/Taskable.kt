package com.lenta.bp12.model

import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.TaskType

interface Taskable{

    val name: String
    val control: ControlType
    val baskets: MutableList<Basket>
    val type: TaskType?

    fun updateBasket(basket: Basket)
    fun removeBaskets(basketList: MutableList<Basket>)
    fun getBasketsByGood(good: Good): List<Basket>
    fun removeEmptyGoods()
    fun removeEmptyBaskets()
}