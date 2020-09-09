package com.lenta.bp12.features.other

import com.lenta.bp12.model.pojo.Basket

data class ItemBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)