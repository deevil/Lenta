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
        return findBatch(batch.batchNumber, batch.materialNumber, batch.processingUnitNumber)
    }

    override fun findBatch(batchNumber: String, materialNumber: String, processingUnitNumber: String): TaskBatchInfo? {
        return batchesInfo.firstOrNull { it.batchNumber == batchNumber && it.materialNumber == materialNumber && it.processingUnitNumber == processingUnitNumber}
    }

    override fun findBatchOfProduct(productInfo: TaskProductInfo): List<TaskBatchInfo>? {
        return findBatchOfProduct(productInfo.materialNumber)
    }

    override fun findBatchOfProduct(materialNumber: String): List<TaskBatchInfo>? {
        return batchesInfo.filter { it.materialNumber == materialNumber}
    }

    override fun addBatch(batch: TaskBatchInfo): Boolean {
        var index = -1
        for (i in batchesInfo.indices) {
            if (batch.materialNumber == batchesInfo[i].materialNumber &&
                    batch.processingUnitNumber == batchesInfo[i].processingUnitNumber &&
                    batch.batchNumber == batchesInfo[i].batchNumber) {
                index = i
            }
        }

        if (index == -1) {
            batchesInfo.add(batch)
            return true
        }
        return false
    }

    override fun updateBatches(newBatches: List<TaskBatchInfo>) {
        clear()
        newBatches.map {
            addBatch(it)
        }
    }

    override fun changeBatch(batch: TaskBatchInfo): Boolean {
        deleteBatch(batch)
        return addBatch(batch)
    }

    override fun deleteBatch(delBatch: TaskBatchInfo): Boolean {
        batchesInfo.map { it }.filter {batch ->
            if (delBatch.materialNumber == batch.materialNumber &&
                    delBatch.processingUnitNumber == batch.processingUnitNumber &&
                    delBatch.batchNumber == batch.batchNumber) {
                batchesInfo.remove(batch)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        batchesInfo.clear()
    }
}