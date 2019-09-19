package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatches
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies

interface ITaskBatchesDiscrepanciesRepository {
    fun getBatchesDiscrepancies(): List<TaskBatchesDiscrepancies>
    fun findBatchDiscrepanciesOfBatch(batch: TaskBatches): List<TaskBatchesDiscrepancies>
    fun addBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchesDiscrepanciesForBatch(batch: TaskBatches): Boolean
    fun getCountAcceptOfBatch(batch: TaskBatches): Double
    fun getCountRefusalOfBatch(batch: TaskBatches): Double
    fun clear()
}