package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskBatchesRepository {
    fun getBatches(): List<TaskBatchInfo>
    fun findBatch(batch: TaskBatchInfo): TaskBatchInfo?
    fun findBatchOfProduct(productInfo: TaskProductInfo): TaskBatchInfo?
    fun addBatch(batch: TaskBatchInfo): Boolean
    fun updateBatches(newBatches: List<TaskBatchInfo>)
    fun changeBatch(batch: TaskBatchInfo): Boolean
    fun deleteBatch(batch: TaskBatchInfo): Boolean
    fun clear()
}