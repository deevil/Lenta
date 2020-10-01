package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampBadRepository
import com.lenta.bp9.model.task.TaskExciseStampBad
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskExciseStampBadRepository : ITaskExciseStampBadRepository {

    private val stampsBad: ArrayList<TaskExciseStampBad> = ArrayList()

    override fun getExciseStampsBad(): List<TaskExciseStampBad> {
        return stampsBad
    }

    override fun findExciseStampBad(findExciesStampBad: TaskExciseStampBad): TaskExciseStampBad? {
        return stampsBad.firstOrNull {
            it.materialNumber == findExciesStampBad.materialNumber &&
                    it.exciseStampCode == findExciesStampBad.exciseStampCode &&
                    it.typeDiscrepancies == findExciesStampBad.typeDiscrepancies
        }
    }

    override fun addExciseStampBad(addExciesStampBad: TaskExciseStampBad): Boolean {
        var index = -1
        for (i in stampsBad.indices) {
            if (addExciesStampBad.exciseStampCode == stampsBad[i].exciseStampCode) {
                index = i
            }
        }

        if (index == -1) {
            stampsBad.add(addExciesStampBad)
            return true
        }
        return false
    }

    override fun updateExciseStampBad(newExciesStampBad: List<TaskExciseStampBad>) {
        clear()
        newExciesStampBad.map {
            addExciseStampBad(it)
        }
    }

    override fun changeExciseStampBad(chExciesStampBad: TaskExciseStampBad): Boolean {
        deleteExciseStampBad(chExciesStampBad)
        return addExciseStampBad(chExciesStampBad)
    }

    override fun deleteExciseStampBad(delExciesStampBad: TaskExciseStampBad): Boolean {
        return stampsBad.removeItemFromListWithPredicate { stamp ->
            delExciesStampBad.exciseStampCode == stamp.exciseStampCode
        }
    }

    override fun deleteExciseStampBadForProduct(materialNumber: String): Boolean {
        return stampsBad.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber
        }
    }

    override fun deleteExciseStampBadForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return stampsBad.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber && stamp.typeDiscrepancies == typeDiscrepancies
        }
    }

    override fun deleteExciseStampBadNotNormForProduct(materialNumber: String): Boolean {
        return stampsBad.removeItemFromListWithPredicate { stamp ->
            stamp.materialNumber == materialNumber && stamp.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun clear() {
        stampsBad.clear()
    }
}
