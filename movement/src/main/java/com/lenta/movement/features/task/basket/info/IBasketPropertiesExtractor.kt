package com.lenta.movement.features.task.basket.info

import com.lenta.movement.models.Basket

interface IBasketPropertiesExtractor {
    suspend fun extractProperties(basket: Basket?): List<BasketProperty>
}