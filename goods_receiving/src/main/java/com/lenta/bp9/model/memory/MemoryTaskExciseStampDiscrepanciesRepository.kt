package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskExciseStampDiscrepanciesRepository : ITaskExciseStampDiscrepanciesRepository {

    private val stampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()

    override fun getExciseStampDiscrepancies(): List<TaskExciseStampDiscrepancies> {
        return stampsDiscrepancies
    }

    override fun findExciseStampsDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskExciseStampDiscrepancies> {
        return findExciseStampsDiscrepanciesOfProduct(product.materialNumber)
    }

    override fun findExciseStampsDiscrepanciesOfProduct(materialNumber: String): List<TaskExciseStampDiscrepancies> {
        return stampsDiscrepancies.filter { stampDiscrepancies ->
            stampDiscrepancies.materialNumber == materialNumber
        }
    }

    override fun addExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        var index = -1
        for (i in stampsDiscrepancies.indices) {
            if (discrepancy.code == stampsDiscrepancies[i].code) {
                index = i
            }
        }

        if (index == -1) {
            stampsDiscrepancies.add(discrepancy)
            return true
        }
        return false
    }

    override fun updateExciseStampsDiscrepancy(newExciseStampDiscrepancies: List<TaskExciseStampDiscrepancies>) {
        clear()
        newExciseStampDiscrepancies.map {
            addExciseStampDiscrepancy(it)
        }
    }

    override fun changeExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        deleteExciseStampDiscrepancy(discrepancy)
        return addExciseStampDiscrepancy(discrepancy)
    }

    override fun deleteExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        return deleteExciseStampDiscrepancy(discrepancy.code)
    }

    override fun deleteExciseStampDiscrepancy(exciseStampCode: String): Boolean {
        return stampsDiscrepancies.removeItemFromListWithPredicate { stamp ->
            exciseStampCode == stamp.code
        }
    }

    override fun deleteExciseStampsDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        return stampsDiscrepancies.removeItemFromListWithPredicate { stamp ->
            product.materialNumber == stamp.materialNumber
        }
    }

    override fun deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return stampsDiscrepancies.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber
                    && stamp.typeDiscrepancies == typeDiscrepancies
        }
    }

    override fun deleteExciseStampsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        return stampsDiscrepancies.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber
                    && stamp.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(materialNumber: String, boxNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Boolean {
        return stampsDiscrepancies.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber
                    && stamp.processingUnitNumber == processingUnitNumber
                    && stamp.boxNumber == boxNumber
                    && stamp.typeDiscrepancies == typeDiscrepancies
        }
    }

    override fun clear() {
        stampsDiscrepancies.clear()
    }
}