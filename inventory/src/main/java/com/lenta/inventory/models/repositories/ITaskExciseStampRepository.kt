package com.lenta.inventory.models.repositories

import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo

interface ITaskExciseStampRepository {
    fun getExciseStamps(): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(materialNumber: String, storePlaceNumber: String, isSet: Boolean): List<TaskExciseStamp>
    fun updateExciseStamps(newStamps: List<TaskExciseStamp>)
    fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean
    fun addExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean
    fun deleteExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean
    fun clear()
    fun get(index: Int): TaskExciseStamp
    fun lenght(): Int
    fun containsStamp(code: String): Boolean
    fun makeSnapshot()
    fun restoreSnapshot()
    fun isChanged(): Boolean
}