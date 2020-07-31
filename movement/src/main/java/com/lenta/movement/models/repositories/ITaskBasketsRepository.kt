package com.lenta.movement.models.repositories

import com.lenta.movement.models.Basket
import com.lenta.movement.models.GoodsSignOfDivision
import com.lenta.movement.models.ProductInfo
import com.lenta.shared.models.core.Supplier

interface ITaskBasketsRepository {

    fun getBasketByIndex(basketIndex: Int): Basket?

    fun getAll(): List<Basket>
    fun getLastIndexOfProduct(product: ProductInfo) : Int

    fun removeBasket(basket: Basket)

    fun removeProductFromAllBaskets(product: ProductInfo)

    fun removeProductFromBasket(basketIndex: Int, product: ProductInfo)

    suspend fun addProduct(product: ProductInfo, supplier: Supplier? = null, count: Int)

    suspend fun getSuitableBasketOrCreate(product: ProductInfo, supplier: Supplier? = null, signOfDiv: Set<GoodsSignOfDivision>): Basket

    fun clear()
}