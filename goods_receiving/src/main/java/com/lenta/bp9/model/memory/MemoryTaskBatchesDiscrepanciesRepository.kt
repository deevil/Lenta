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
            if (batch.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    batch.setMaterialNumber == batchesDiscrepancies[i].setMaterialNumber &&
                    batch.processingUnitNumber == batchesDiscrepancies[i].processingUnitNumber &&
                    batch.batchNumber == batchesDiscrepancies[i].batchNumber) {
                foundDiscrepancies.add(batchesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean {
        var index = -1
        for (i in batchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    discrepancies.setMaterialNumber == batchesDiscrepancies[i].setMaterialNumber &&
                    discrepancies.processingUnitNumber == batchesDiscrepancies[i].processingUnitNumber &&
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
        batchesDiscrepancies.clear()
        newBatchesDiscrepancies.map {
            addBatchDiscrepancies(it)
        }
    }

    override fun changeBatchDiscrepancy(discrepancy: TaskBatchesDiscrepancies): Boolean {
        deleteBatchDiscrepancies(discrepancy)
        return addBatchDiscrepancies(discrepancy)
    }

    override fun deleteBatchDiscrepancies(discrepancies: TaskBatchesDiscrepancies): Boolean {
        batchesDiscrepancies.map { it }.filter {batchDiscrepancies ->
            if (discrepancies.materialNumber == batchDiscrepancies.materialNumber &&
                    discrepancies.setMaterialNumber == batchDiscrepancies.setMaterialNumber &&
                    discrepancies.processingUnitNumber == batchDiscrepancies.processingUnitNumber &&
                    discrepancies.batchNumber == batchDiscrepancies.batchNumber &&
                    discrepancies.typeDiscrepancies == batchDiscrepancies.typeDiscrepancies) {
                batchesDiscrepancies.remove(discrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteBatchesDiscrepanciesForBatch(batch: TaskBatchInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskBatchesDiscrepancies>()
        for (i in batchesDiscrepancies.indices) {
            if (batch.materialNumber == batchesDiscrepancies[i].materialNumber &&
                    batch.setMaterialNumber == batchesDiscrepancies[i].setMaterialNumber &&
                    batch.processingUnitNumber == batchesDiscrepancies[i].processingUnitNumber &&
                    batch.batchNumber == batchesDiscrepancies[i].batchNumber) {
                delDiscrepancies.add(batchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteBatchDiscrepancies(it)
        }
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

    override fun getCountBatchNotProcessedOfBatch(batch: TaskBatchInfo): Double {
        return 0.0 //batch.planQuantityBatch.toDouble() - getCountAcceptOfBatch(batch) - getCountRefusalOfBatch(batch)
    }

    override fun clear() {
        batchesDiscrepancies.clear()
    }
}