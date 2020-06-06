package com.lenta.movement.platform

import com.lenta.movement.models.*

interface IFormatter {

    fun formatMarketName(market: String): String

    fun getProductName(product: ProductInfo): String

    fun getTaskTypeNameDescription(taskType: TaskType): String

    fun getTaskStatusName(taskStatus: Task.Status): String

    fun getMovementTypeNameDescription(movementType: MovementType): String

    fun getBasketName(basket: Basket): String

    fun getBasketDescription(basket: Basket, task: Task, settings: TaskSettings): String

    fun basketGisControl(basket: Basket): String

}