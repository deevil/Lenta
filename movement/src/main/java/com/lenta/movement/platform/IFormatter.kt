package com.lenta.movement.platform

import com.lenta.movement.models.*

interface IFormatter {

    fun getProductName(product: ProductInfo): String

    fun getTaskTypeNameDescription(taskType: TaskType): String

    fun getMovementTypeNameDescription(movementType: MovementType): String

    fun getBasketName(basket: Basket): String

    fun getBasketDescription(basket: Basket, task: Task): String

}