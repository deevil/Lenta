package com.lenta.bp12.features.create_task.task_composition

import com.lenta.bp12.model.pojo.create_task.Basket

data class ItemCommonBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)