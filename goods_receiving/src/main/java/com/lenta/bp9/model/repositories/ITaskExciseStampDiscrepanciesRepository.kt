package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies

interface ITaskExciseStampDiscrepanciesRepository {
    fun getExciseStampDiscrepancies(): List<TaskExciseStampDiscrepancies>
    fun findExciseStampsDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskExciseStampDiscrepancies>
    fun findExciseStampsDiscrepanciesOfProduct(materialNumber: String): List<TaskExciseStampDiscrepancies>
    fun addExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean
    fun updateExciseStampsDiscrepancy(newExciseStampDiscrepancies: List<TaskExciseStampDiscrepancies>)
    fun changeExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean
    fun deleteExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean
    fun deleteExciseStampDiscrepancy(exciseStampCode: String): Boolean
    fun deleteExciseStampsDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(materialNumber: String, boxNumber: String, typeDiscrepancies: String): Boolean
    fun clear()
}