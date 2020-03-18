package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampBadRepository
import com.lenta.bp9.model.task.TaskExciseStampBad

class MemoryTaskExciseStampBadRepository : ITaskExciseStampBadRepository {

    private val stampsBad: ArrayList<TaskExciseStampBad> = ArrayList()

    override fun getExciseStampsBad(): List<TaskExciseStampBad> {
        return stampsBad
    }

    override fun findExciseStampBad(findExciesStampBad: TaskExciseStampBad): TaskExciseStampBad? {
        return stampsBad.firstOrNull { it.materialNumber == findExciesStampBad.materialNumber &&
                it.exciseStampCode == findExciesStampBad.exciseStampCode &&
                it.typeDiscrepancies == findExciesStampBad.typeDiscrepancies
        }
    }

    override fun addExciseStampBad(addExciesStampBad: TaskExciseStampBad): Boolean {
        var index = -1
        for (i in stampsBad.indices) {
            if (addExciesStampBad.materialNumber == stampsBad[i].materialNumber &&
                    addExciesStampBad.exciseStampCode == stampsBad[i].exciseStampCode &&
                    addExciesStampBad.typeDiscrepancies == stampsBad[i].typeDiscrepancies) {
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
        stampsBad.clear()
        newExciesStampBad.map {
            addExciseStampBad(it)
        }
    }

    override fun changeExciseStampBad(chExciesStampBad: TaskExciseStampBad): Boolean {
        deleteExciseStampBad(chExciesStampBad)
        return addExciseStampBad(chExciesStampBad)
    }

    override fun deleteExciseStampBad(delExciesStampBad: TaskExciseStampBad): Boolean {
        var index = -1
        for (i in stampsBad.indices) {
            if (delExciesStampBad.materialNumber == stampsBad[i].materialNumber &&
                    delExciesStampBad.exciseStampCode == stampsBad[i].exciseStampCode &&
                    delExciesStampBad.typeDiscrepancies == stampsBad[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        stampsBad.removeAt(index)
        return true
    }

    override fun clear() {
        stampsBad.clear()
    }
}