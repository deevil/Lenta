package com.lenta.movement.platform

import com.lenta.movement.models.*

interface IFormatter {

    fun formatMarketName(market: String): String

    fun getProductName(product: ProductInfo): String

    fun getTaskTypeNameDescription(taskType: TaskType): String

    fun getTaskStatusName(taskStatus: Task.Status): String

    fun getBasketName(basket: Basket): String

    fun getBasketDescription(basket: Basket, task: Task, settings: TaskSettings): String

    fun getBasketTitle(basket: Basket, task: Task, taskSettings: TaskSettings) : String

    fun basketGisControl(basket: Basket): Int

    fun getEOSubtitle(eo: ProcessingUnit): String
    fun getEOSubtitleForInsides(eo: ProcessingUnit): String

    fun getGETitle(ge: CargoUnit): String

    fun getOrderUnitsNameByCode(orderUnits: String): String

    fun getTaskTitle(task: Task): String
}