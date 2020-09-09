package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskZBatchesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS

class MemoryTaskZBatchesDiscrepanciesRepository : ITaskZBatchesDiscrepanciesRepository {

    private val zBatchesDiscrepancies: ArrayList<TaskZBatchesDiscrepancies> = ArrayList()

    override fun getZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies> {
        return zBatchesDiscrepancies
    }

    override fun findZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): List<TaskZBatchesDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskZBatchesDiscrepancies>()
        for (i in zBatchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == zBatchesDiscrepancies[i].materialNumber
                    && discrepancies.manufactureCode == zBatchesDiscrepancies[i].manufactureCode
                    && discrepancies.shelfLifeDate == zBatchesDiscrepancies[i].shelfLifeDate
                    && discrepancies.shelfLifeTime == zBatchesDiscrepancies[i].shelfLifeTime) {
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

    override fun getCountAcceptOfZBatch(discrepancies: TaskZBatchesDiscrepancies): Double {
        var countAccept = 0.0
        findZBatchDiscrepancies(discrepancies)
                .filter {
                    it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                }.map {disc ->
                    countAccept += disc.numberDiscrepancies.toDouble()
                }
        return countAccept
    }

    override fun getCountAcceptOfZBatchPGE(discrepancies: TaskZBatchesDiscrepancies): Double {
        var countAccept = 0.0
        findZBatchDiscrepancies(discrepancies)
                .filter {
                    it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                            || it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
                }.map {disc ->
                    countAccept += disc.numberDiscrepancies.toDouble()
                }
        return countAccept
    }

    override fun clear() {
        zBatchesDiscrepancies.clear()
    }
}