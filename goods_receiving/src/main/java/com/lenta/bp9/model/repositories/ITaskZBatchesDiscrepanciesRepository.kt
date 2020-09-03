package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies

interface ITaskZBatchesDiscrepanciesRepository {
    fun getZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies>
    fun findZBatchDiscrepanciesOfZBatch(zBatch: TaskZBatchInfo): List<TaskZBatchesDiscrepancies>
    fun findZBatchDiscrepanciesOfProduct(materialNumber: String): List<TaskZBatchesDiscrepancies>
    fun findZBatchDiscrepanciesOfProducts(materialNumbers: List<String>): List<TaskZBatchesDiscrepancies>
    fun addZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean
    fun updateZBatchesDiscrepancy(newBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>)
    fun changeZBatchDiscrepancy(discrepancy: TaskZBatchesDiscrepancies): Boolean
    fun deleteZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean
    fun deleteZBatchesDiscrepanciesForZBatch(batch: TaskZBatchInfo): Boolean
    fun deleteZBatchesDiscrepanciesForProduct(materialNumber: String): Boolean
    fun deleteZBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean
    fun clear()
}