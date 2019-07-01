package com.lenta.inventory.models.repositories

import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.shared.models.core.ProductInfo

interface ITaskExciseStampRepository {
    fun getExciseStamps(): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(product: ProductInfo): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(materialNumber: String): List<TaskExciseStamp>
    fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun deleteExciseStampsForProduct(product: ProductInfo): Boolean
    fun addExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean
    fun deleteExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean
    fun clear()
    fun get(index: Int): TaskExciseStamp
    fun lenght(): Int
    fun containsStamp(code: String): Boolean
}