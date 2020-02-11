package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskExciseStamp
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskExciseStampRepository {
    fun getExciseStamps(): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStamp>
    fun findExciseStampsOfProduct(materialNumber: String): List<TaskExciseStamp>
    fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun addExciseStamps(exciseStamps: List<TaskExciseStamp>): Boolean
    fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean
    fun deleteExciseStamps(exciseStamps: List<TaskExciseStamp>): Boolean
    fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean
    fun clear()
}