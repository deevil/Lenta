package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchInfo

interface ITaskBatchesRepository {
    fun getBatches(): List<TaskBatchInfo>
    fun findBatch(batch: TaskBatchInfo): TaskBatchInfo?
    fun addBatch(batch: TaskBatchInfo): Boolean
    fun changeBatch(batch: TaskBatchInfo): Boolean
    fun deleteBatch(batch: TaskBatchInfo): Boolean
    fun clear()
}