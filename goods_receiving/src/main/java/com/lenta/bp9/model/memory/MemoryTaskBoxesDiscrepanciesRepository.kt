package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBoxesDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskBoxDiscrepancies
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

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
        clear()
        newBoxesDiscrepancies.map {
            addBoxDiscrepancies(it)
        }
    }

    override fun changeBoxDiscrepancy(discrepancy: TaskBoxDiscrepancies): Boolean {
        deleteBoxDiscrepancies(discrepancy)
        return addBoxDiscrepancies(discrepancy)
    }

    override fun deleteBoxDiscrepancies(discrepancies: TaskBoxDiscrepancies): Boolean {
        return deleteBoxDiscrepancies(discrepancies.materialNumber, discrepancies.boxNumber, discrepancies.typeDiscrepancies, discrepancies.processingUnitNumber)
    }

    override fun deleteBoxDiscrepancies(materialNumber: String, boxNumber: String, typeDiscrepancies: String, processingUnitNumber: String): Boolean {
        return boxesDiscrepancies.removeItemFromListWithPredicate { box ->
            box.materialNumber == materialNumber
                    && box.processingUnitNumber == processingUnitNumber
                    && box.boxNumber == boxNumber
                    && box.typeDiscrepancies == typeDiscrepancies
        }
    }

    override fun deleteBoxesDiscrepanciesForBox(delBox: TaskBoxInfo): Boolean {
        return boxesDiscrepancies.removeItemFromListWithPredicate { box ->
            delBox.materialNumber == box.materialNumber &&
                    delBox.boxNumber == box.boxNumber
        }
    }

    override fun deleteBoxesDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        return boxesDiscrepancies.removeItemFromListWithPredicate { box ->
            box.materialNumber == product.materialNumber
        }
    }

    override fun deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return boxesDiscrepancies.removeItemFromListWithPredicate { box ->
            materialNumber == box.materialNumber &&
                    typeDiscrepancies == box.typeDiscrepancies
        }
    }

    override fun deleteBoxesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        return boxesDiscrepancies.removeItemFromListWithPredicate { box ->
            box.materialNumber == materialNumber &&
                    box.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun clear() {
        boxesDiscrepancies.clear()
    }
}