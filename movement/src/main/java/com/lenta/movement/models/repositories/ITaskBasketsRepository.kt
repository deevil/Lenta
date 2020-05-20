package com.lenta.movement.models.repositories

import com.lenta.movement.models.Basket
import com.lenta.movement.models.ProductInfo
import com.lenta.shared.models.core.Supplier

interface ITaskBasketsRepository {

    fun getBasketByIndex(basketIndex: Int): Basket

    fun getAll(): List<Basket>

    fun removeBasket(basket: Basket)

    fun removeProductFromAllBaskets(product: ProductInfo)

    fun removeProduct(basketIndex: Int, product: ProductInfo)

    fun addOrReplaceIfExist(basket: Basket)

    fun getSuitableBasketOrCreate(productInfo: ProductInfo, supplier: Supplier? = null): Basket
}