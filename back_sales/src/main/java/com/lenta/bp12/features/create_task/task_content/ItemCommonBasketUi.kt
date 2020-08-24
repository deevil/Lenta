package com.lenta.bp12.features.create_task.task_content

import com.lenta.bp12.model.pojo.Basket

data class ItemCommonBasketUi(
        val basket: Basket,
        val position: String,
        val name: String,
        val description: String,
        val quantity: String
)