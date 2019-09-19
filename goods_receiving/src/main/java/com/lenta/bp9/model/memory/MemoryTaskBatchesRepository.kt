package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBatchesRepository
import com.lenta.bp9.model.task.TaskBatchesInfo

class MemoryTaskBatchesRepository : ITaskBatchesRepository {

    private val batchesInfo: ArrayList<TaskBatchesInfo> = ArrayList()

    override fun getBatches(): List<TaskBatchesInfo> {
        return batchesInfo
    }

    override fun findBatch(batch: TaskBatchesInfo): TaskBatchesInfo? {
        return batchesInfo.firstOrNull { it.materialNumber == batch.materialNumber && it.batchNumber == batch.batchNumber}
    }

    override fun addBatch(batch: TaskBatchesInfo): Boolean {
        var index = -1
        for (i in batchesInfo.indices) {
            if (batch.materialNumber == batchesInfo[i].materialNumber && batch.batchNumber == batchesInfo[i].batchNumber) {
                index = i
            }
        }

        if (index == -1) {
            batchesInfo.add(batch)
            return true
        }
        return false
    }

    override fun changeBatch(batch: TaskBatchesInfo): Boolean {
        deleteBatch(batch)
        return addBatch(batch)
    }

    override fun deleteBatch(batch: TaskBatchesInfo): Boolean {
        var index = -1
        for (i in batchesInfo.indices) {
            if (batch.materialNumber == batchesInfo[i].materialNumber && batch.batchNumber == batchesInfo[i].batchNumber) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        batchesInfo.removeAt(index)
        return true
    }

    override fun clear() {
        batchesInfo.clear()
    }
}