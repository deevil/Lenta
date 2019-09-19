package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBatches

interface ITaskBatchesRepository {
    fun getBatches(): List<TaskBatches>
    fun findBatch(batch: TaskBatches): TaskBatches?
    fun addBatch(batch: TaskBatches): Boolean
    fun changeBatch(batch: TaskBatches): Boolean
    fun deleteBatch(batch: TaskBatches): Boolean
    fun clear()
}