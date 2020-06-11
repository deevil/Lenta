package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies

interface ITaskBatchesDiscrepanciesRepository {
    fun getBatchesDiscrepancies(): List<TaskBatchesDiscrepancies>
    fun findBatchDiscrepanciesOfBatch(batch: TaskBatchInfo): List<TaskBatchesDiscrepancies>
    fun findBatchDiscrepanciesOfProduct(materialNumber: String): List<TaskBatchesDiscrepancies>
    fun addBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun updateBatchesDiscrepancy(newBatchesDiscrepancies: List<TaskBatchesDiscrepancies>)
    fun changeBatchDiscrepancy(discrepancy: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean
    fun deleteBatchesDiscrepanciesForBatch(batch: TaskBatchInfo): Boolean
    fun deleteBatchesDiscrepanciesForProduct(materialNumber: String): Boolean
    fun deleteBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun deleteBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean
    fun getCountAcceptOfBatch(batch: TaskBatchInfo): Double
    fun getCountRefusalOfBatch(batch: TaskBatchInfo): Double
    fun getCountBatchNotProcessedOfBatch(batch: TaskBatchInfo): Double
    fun getCountAcceptOfBatchPGE(batch: TaskBatchInfo): Double
    fun getCountRefusalOfBatchPGE(batch: TaskBatchInfo): Double
    fun getCountBatchNotProcessedOfBatchPGE(batch: TaskBatchInfo): Double
    fun clear()
}