package com.lenta.bp12.model

import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good

interface Taskable{

    val name: String
    val control: ControlType
    val baskets: MutableList<Basket>

    fun updateBasket(basket: Basket)
    fun removeBaskets(basketList: MutableList<Basket>)
    fun getBasketsByGood(good: Good): List<Basket>
    fun removeEmptyGoods()
    fun removeEmptyBaskets()
}