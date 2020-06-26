package com.lenta.bp12.features.other

import com.lenta.bp12.model.pojo.create_task.Basket

data class ItemBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)