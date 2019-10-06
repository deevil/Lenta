package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBatchesRepository
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskProductInfo

class MemoryTaskBatchesRepository : ITaskBatchesRepository {

    private val batchesInfo: ArrayList<TaskBatchInfo> = ArrayList()

    override fun getBatches(): List<TaskBatchInfo> {
        return batchesInfo
    }

    override fun findBatch(batch: TaskBatchInfo): TaskBatchInfo? {
        return batchesInfo.firstOrNull { it.materialNumber == batch.materialNumber && it.batchNumber == batch.batchNumber}
    }

    override fun findBatchOfProduct(productInfo: TaskProductInfo): TaskBatchInfo? {
        return batchesInfo.firstOrNull { it.materialNumber == productInfo.materialNumber}
    }

    override fun addBatch(batch: TaskBatchInfo): Boolean {
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

    override fun changeBatch(batch: TaskBatchInfo): Boolean {
        deleteBatch(batch)
        return addBatch(batch)
    }

    override fun deleteBatch(batch: TaskBatchInfo): Boolean {
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