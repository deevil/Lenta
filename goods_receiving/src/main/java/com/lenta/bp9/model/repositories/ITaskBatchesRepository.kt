package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatchesInfo

interface ITaskBatchesRepository {
    fun getBatches(): List<TaskBatchesInfo>
    fun findBatch(batch: TaskBatchesInfo): TaskBatchesInfo?
    fun addBatch(batch: TaskBatchesInfo): Boolean
    fun changeBatch(batch: TaskBatchesInfo): Boolean
    fun deleteBatch(batch: TaskBatchesInfo): Boolean
    fun clear()
}