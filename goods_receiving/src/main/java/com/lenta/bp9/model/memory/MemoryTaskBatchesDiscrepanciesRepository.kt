package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBatchesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies

class MemoryTaskBatchesDiscrepanciesRepository : ITaskBatchesDiscrepanciesRepository {

    private val batchesDiscrepancies: ArrayList<TaskBatchesDiscrepancies> = ArrayList()

    override fun getBatchesDiscrepancies(): List<TaskBatchesDiscrepancies> {
        return batchesDiscrepancies
    }

    override fun findBatchDiscrepanciesOfBatch(batch: TaskBatchInfo): List<TaskBatchesDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBatchesDiscrepancies>()
        for (i in batchesDiscrepancies.indices) {
            if (batch.materialNumber == batchesDiscrepancies[i].materialNumber && batch.batchNumber == batchesDiscrepancies[i].batchNumber) {
                foundDiscrepancies.add(batchesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean {
        var index = -1
        for (i in batchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    discrepancies.batchNumber == batchesDiscrepancies[i].batchNumber &&
                    discrepancies.typeDiscrepancies == batchesDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            batchesDiscrepancies.add(discrepancies)
            return true
        }
        return false
    }

    override fun updateBatchesDiscrepancy(newBatchesDiscrepancies: List<TaskBatchesDiscrepancies>) {
        for (batchesDiscrepancies in newBatchesDiscrepancies) {
            addBatchDiscrepancies(batchesDiscrepancies)
        }
    }

    override fun changeBatchDiscrepancy(discrepancy: TaskBatchesDiscrepancies): Boolean {
        deleteBatchDiscrepancies(discrepancy)
        return addBatchDiscrepancies(discrepancy)
    }

    override fun deleteBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean {
        var index = -1
        for (i in batchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    discrepancies.batchNumber == batchesDiscrepancies[i].batchNumber &&
                    discrepancies.typeDiscrepancies == batchesDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        batchesDiscrepancies.removeAt(index)
        return true
    }

    override fun deleteBatchesDiscrepanciesForBatch(batch: TaskBatchInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskBatchesDiscrepancies>()
        for (i in batchesDiscrepancies.indices) {
            if (batch.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    batch.batchNumber == batchesDiscrepancies[i].batchNumber) {
                delDiscrepancies.add(batchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        batchesDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun getCountAcceptOfBatch(batch: TaskBatchInfo): Double {
        var countAccept = 0.0
        findBatchDiscrepanciesOfBatch(batch).filter {
            it.typeDiscrepancies == "1"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies.toDouble()
        }
        return countAccept
    }

    override fun getCountRefusalOfBatch(batch: TaskBatchInfo): Double {
        var countRefusal = 0.0
        findBatchDiscrepanciesOfBatch(batch).filter {
            it.typeDiscrepancies != "1"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }

    override fun clear() {
        batchesDiscrepancies.clear()
    }
}