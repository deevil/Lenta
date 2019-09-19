package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchesInfo
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies

interface ITaskBatchesDiscrepanciesRepository {
    fun getBatchesDiscrepancies(): List<TaskBatchesDiscrepancies>
    fun findBatchDiscrepanciesOfBatch(batch: TaskBatchesInfo): List<TaskBatchesDiscrepancies>
    fun addBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchesDiscrepanciesForBatch(batch: TaskBatchesInfo): Boolean
    fun getCountAcceptOfBatch(batch: TaskBatchesInfo): Double
    fun getCountRefusalOfBatch(batch: TaskBatchesInfo): Double
    fun clear()
}