package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskZBatchesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies

class MemoryTaskZBatchesDiscrepanciesRepository : ITaskZBatchesDiscrepanciesRepository {

    private val zBatchesDiscrepancies: ArrayList<TaskZBatchesDiscrepancies> = ArrayList()

    override fun getZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies> {
        return zBatchesDiscrepancies
    }

    override fun findZBatchDiscrepanciesOfZBatch(zBatch: TaskZBatchInfo): List<TaskZBatchesDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (zBatch.materialNumber == zBatchesDiscrepancies[i].materialNumber &&
                    zBatch.processingUnit == zBatchesDiscrepancies[i].processingUnit &&
                    zBatch.batchNumber == zBatchesDiscrepancies[i].batchNumber) {
                foundDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun findZBatchDiscrepanciesOfProduct(materialNumber: String): List<TaskZBatchesDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (materialNumber == zBatchesDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun findZBatchDiscrepanciesOfProducts(materialNumbers: List<String>): List<TaskZBatchesDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        materialNumbers.map {
            for (i in zBatchesDiscrepancies.indices) {
                if (it == zBatchesDiscrepancies[i].materialNumber) {
                    foundDiscrepancies.add(zBatchesDiscrepancies[i])
                }
            }
        }
        return foundDiscrepancies
    }

    override fun addZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean {
        var index = -1
        for (i in zBatchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == zBatchesDiscrepancies[i].materialNumber &&
                    discrepancies.processingUnit == zBatchesDiscrepancies[i].processingUnit &&
                    discrepancies.batchNumber == zBatchesDiscrepancies[i].batchNumber &&
                    discrepancies.typeDiscrepancies == zBatchesDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            zBatchesDiscrepancies.add(discrepancies)
            return true
        }
        return false
    }

    override fun updateZBatchesDiscrepancy(newZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>) {
        zBatchesDiscrepancies.clear()
        newZBatchesDiscrepancies.map {
            addZBatchDiscrepancies(it)
        }
    }

    override fun changeZBatchDiscrepancy(discrepancy: TaskZBatchesDiscrepancies): Boolean {
        deleteZBatchDiscrepancies(discrepancy)
        return addZBatchDiscrepancies(discrepancy)
    }

    override fun deleteZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean {
        zBatchesDiscrepancies.map { it }.filter { batchDiscrepancies ->
            if (discrepancies.materialNumber == batchDiscrepancies.materialNumber &&
                    discrepancies.processingUnit == batchDiscrepancies.processingUnit &&
                    discrepancies.batchNumber == batchDiscrepancies.batchNumber &&
                    discrepancies.typeDiscrepancies == batchDiscrepancies.typeDiscrepancies) {
                zBatchesDiscrepancies.remove(batchDiscrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteZBatchesDiscrepanciesForZBatch(batch: TaskZBatchInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (batch.materialNumber == zBatchesDiscrepancies[i].materialNumber &&
                    batch.processingUnit == zBatchesDiscrepancies[i].processingUnit &&
                    batch.batchNumber == zBatchesDiscrepancies[i].batchNumber) {
                delDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteZBatchDiscrepancies(it)
        }
        return true
    }

    override fun deleteZBatchesDiscrepanciesForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (materialNumber == zBatchesDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteZBatchDiscrepancies(it)
        }
        return true
    }

    override fun deleteZBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (materialNumber == zBatchesDiscrepancies[i].materialNumber && zBatchesDiscrepancies[i].typeDiscrepancies != "1") {
                delDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteZBatchDiscrepancies(it)
        }
        return true
    }

    override fun deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        val delDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (materialNumber == zBatchesDiscrepancies[i].materialNumber &&
                    typeDiscrepancies == zBatchesDiscrepancies[i].typeDiscrepancies) {
                delDiscrepancies.add(zBatchesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteZBatchDiscrepancies(it)
        }
        return true
    }

    override fun clear() {
        zBatchesDiscrepancies.clear()
    }
}