package com.lenta.bp12.features.basket

import com.lenta.bp12.model.pojo.create_task.Basket

data class ItemWholesaleBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String,
        val isPrinted: Boolean,
        val isLocked: Boolean
)