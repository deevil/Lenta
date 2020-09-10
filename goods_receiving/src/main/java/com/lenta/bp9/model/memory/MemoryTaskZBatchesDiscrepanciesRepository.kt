package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskZBatchesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskBatchesDiscrepancies
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

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

    override fun addZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean {
        var index = -1
        for (i in zBatchesDiscrepancies.indices) {
            if (discrepancies.materialNumber == zBatchesDiscrepancies[i].materialNumber
                    && discrepancies.manufactureCode == zBatchesDiscrepancies[i].manufactureCode
                    && discrepancies.shelfLifeDate == zBatchesDiscrepancies[i].shelfLifeDate
                    && discrepancies.shelfLifeTime == zBatchesDiscrepancies[i].shelfLifeTime) {
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
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber ==discrepancies.materialNumber
                    && it.manufactureCode == discrepancies.manufactureCode
                    && it.shelfLifeDate == discrepancies.shelfLifeDate
                    && it.shelfLifeTime == discrepancies.shelfLifeTime
        }
    }

    override fun deleteZBatchesDiscrepanciesForProduct(materialNumber: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
        }
    }

    override fun deleteZBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return zBatchesDiscrepancies.removeItemFromListWithPredicate {
            it.materialNumber == materialNumber
                    && it.typeDiscrepancies == typeDiscrepancies
        }
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