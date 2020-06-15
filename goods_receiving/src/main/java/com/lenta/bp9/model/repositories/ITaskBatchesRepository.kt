package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskBatchesRepository {
    fun getBatches(): List<TaskBatchInfo>
    fun findBatch(batch: TaskBatchInfo): TaskBatchInfo?
    fun findBatch(batchNumber: String, materialNumber: String, processingUnitNumber: String): TaskBatchInfo?
    fun findBatchOfProduct(productInfo: TaskProductInfo): List<TaskBatchInfo>?
    fun findBatchOfProduct(materialNumber: String): List<TaskBatchInfo>?
    fun addBatch(batch: TaskBatchInfo): Boolean
    fun updateBatches(newBatches: List<TaskBatchInfo>)
    fun changeBatch(batch: TaskBatchInfo): Boolean
    fun deleteBatch(delBatch: TaskBatchInfo): Boolean
    fun clear()
}