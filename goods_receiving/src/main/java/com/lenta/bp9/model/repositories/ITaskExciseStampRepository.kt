package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskExciseStampRepository {
    fun getExciseStamps(): List<TaskExciseStampInfo>
    fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStampInfo>
    fun findExciseStampsOfProduct(materialNumber: String): List<TaskExciseStampInfo>
    fun addExciseStamp(exciseStamp: TaskExciseStampInfo): Boolean
    fun addExciseStamps(exciseStamps: List<TaskExciseStampInfo>): Boolean
    fun updateExciseStamps(newExciseStamps: List<TaskExciseStampInfo>)
    fun changeExciseStamps(exciseStamp: TaskExciseStampInfo): Boolean
    fun deleteExciseStamp(exciseStamp: TaskExciseStampInfo): Boolean
    fun deleteExciseStamps(exciseStamps: List<TaskExciseStampInfo>): Boolean
    fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean
    fun clear()
}