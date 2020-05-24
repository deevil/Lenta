package com.lenta.movement.models.repositories

import com.lenta.movement.models.Basket
import com.lenta.movement.models.ProductInfo
import com.lenta.shared.models.core.Supplier

interface ITaskBasketsRepository {

    fun getBasketByIndex(basketIndex: Int): Basket

    fun getAll(): List<Basket>

    fun removeBasket(basket: Basket)

    fun removeProductFromAllBaskets(product: ProductInfo)

    fun removeProductFromBasket(basketIndex: Int, product: ProductInfo)

    fun addProduct(product: ProductInfo, supplier: Supplier? = null, count: Int)

    fun getSuitableBasketOrCreate(productInfo: ProductInfo, supplier: Supplier? = null): Basket

    fun clear()
}