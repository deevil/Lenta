package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

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
    fun deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteExciseStampsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(materialNumber: String, boxNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Boolean
    fun clear()
}