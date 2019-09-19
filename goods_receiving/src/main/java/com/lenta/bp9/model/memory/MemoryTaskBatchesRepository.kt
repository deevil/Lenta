package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBatchesRepository
import com.lenta.bp9.model.task.TaskBatches

class MemoryTaskBatchesRepository : ITaskBatchesRepository {

    private val batchesInfo: ArrayList<TaskBatches> = ArrayList()

    override fun getBatches(): List<TaskBatches> {
        return batchesInfo
    }

    override fun findBatch(batch: TaskBatches): TaskBatches? {
        return batchesInfo.firstOrNull { it.materialNumber == batch.materialNumber && it.batchNumber == batch.batchNumber}
    }

    override fun addBatch(batch: TaskBatches): Boolean {
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

    override fun changeBatch(batch: TaskBatches): Boolean {
        deleteBatch(batch)
        return addBatch(batch)
    }

    override fun deleteBatch(batch: TaskBatches): Boolean {
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