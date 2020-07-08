package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBoxesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBoxDiscrepancies
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants

class MemoryTaskBoxesDiscrepanciesRepository : ITaskBoxesDiscrepanciesRepository {

    private val boxesDiscrepancies: ArrayList<TaskBoxDiscrepancies> = ArrayList()

    override fun getBoxesDiscrepancies(): List<TaskBoxDiscrepancies> {
        return boxesDiscrepancies
    }

    override fun findBoxesDiscrepanciesOfBox(box: TaskBoxInfo): List<TaskBoxDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (box.boxNumber == boxesDiscrepancies[i].boxNumber) {
                foundDiscrepancies.add(boxesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun findBoxesDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskBoxDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (product.materialNumber == boxesDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(boxesDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean {
        var index = -1
        for (i in boxesDiscrepancies.indices) {
            if (discrepancies.materialNumber == boxesDiscrepancies[i].materialNumber &&
                    discrepancies.boxNumber == boxesDiscrepancies[i].boxNumber &&
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

    override fun updateBoxesDiscrepancies(newBoxesDiscrepancies: List<TaskBoxDiscrepancies>) {
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
        return deleteBoxDiscrepancies(discrepancies.materialNumber, discrepancies.boxNumber, discrepancies.typeDiscrepancies)
    }

    override fun deleteBoxDiscrepancies(materialNumber: String, boxNumber: String, typeDiscrepancies: String): Boolean {
        boxesDiscrepancies.map { it }.filter { boxDiscrepancies ->
            if (materialNumber == boxDiscrepancies.materialNumber &&
                    boxNumber == boxDiscrepancies.boxNumber &&
                    typeDiscrepancies == boxDiscrepancies.typeDiscrepancies) {
                boxesDiscrepancies.remove(boxDiscrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteBoxesDiscrepanciesForBox(delBox: TaskBoxInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (delBox.materialNumber == boxesDiscrepancies[i].materialNumber &&
                    delBox.boxNumber == boxesDiscrepancies[i].boxNumber) {
                delDiscrepancies.add(boxesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteBoxDiscrepancies(it)
        }
        return true
    }

    override fun deleteBoxesDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (product.materialNumber == boxesDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(boxesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteBoxDiscrepancies(it)
        }
        return true
    }

    override fun deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        val delDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (materialNumber == boxesDiscrepancies[i].materialNumber &&
                    typeDiscrepancies == boxesDiscrepancies[i].typeDiscrepancies) {
                delDiscrepancies.add(boxesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteBoxDiscrepancies(it)
        }
        return true
    }

    override fun deleteBoxesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskBoxDiscrepancies>()
        for (i in boxesDiscrepancies.indices) {
            if (boxesDiscrepancies[i].materialNumber == materialNumber &&
                    boxesDiscrepancies[i].typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                delDiscrepancies.add(boxesDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteBoxDiscrepancies(it)
        }
        return true
    }

    override fun clear() {
        boxesDiscrepancies.clear()
    }
}