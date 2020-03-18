package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBoxesDiscrepanciesRepository
import com.lenta.bp9.model.repositories.ITaskExciseStampDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBoxDiscrepancies
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies

class MemoryTaskBoxesDiscrepanciesRepository : ITaskBoxesDiscrepanciesRepository {

    private val boxesDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()

    override fun getBoxesDiscrepancies(): List<TaskBoxDiscrepancies> {
        return boxesDiscrepancies
    }

    override fun findDiscrepanciesOfBox(findBox: TaskBoxInfo): List<TaskBoxDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (findBox.boxNumber == boxesDiscrepancies[i].boxNumber) {
                foundDiscrepancies.add(boxesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean {
        var index = -1
        for (i in boxesDiscrepancies.indices) {
            if (discrepancies.materialNumber == boxesDiscrepancies[i].materialNumber &&
                    /**discrepancies.boxNumber == boxesDiscrepancies[i].boxNumber &&*/
                    discrepancies.typeDiscrepancies == boxesDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            boxesDiscrepancies.add(discrepancies)
            return true
        }
        return false
    }

    override fun updateBoxesDiscrepancy(newBoxesDiscrepancies: List<TaskBoxDiscrepancies>) {
        boxesDiscrepancies.clear()
        newBoxesDiscrepancies.map {
            addBoxDiscrepancies(it)
        }
    }

    override fun changeBoxDiscrepancy(discrepancy: TaskBoxDiscrepancies): Boolean {
        deleteBoxDiscrepancies(discrepancy)
        return addBoxDiscrepancies(discrepancy)
    }

    override fun deleteBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean {
        var index = -1
        for (i in boxesDiscrepancies.indices) {
            if (discrepancies.materialNumber == boxesDiscrepancies[i].materialNumber &&
                    /**discrepancies.boxNumber == boxesDiscrepancies[i].boxNumber &&*/
                    discrepancies.typeDiscrepancies == boxesDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        boxesDiscrepancies.removeAt(index)
        return true
    }

    override fun deleteBoxesDiscrepanciesForBox(delBox: TaskBoxInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (delBox.materialNumber == boxesDiscrepancies[i].materialNumber && delBox.boxNumber == boxesDiscrepancies[i].boxNumber) {
                delDiscrepancies.add(boxesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        boxesDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun clear() {
        boxesDiscrepancies.clear()
    }
}