package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies

class MemoryTaskExciseStampDiscrepanciesRepository : ITaskExciseStampDiscrepanciesRepository {

    private val stampsDiscrepancies: ArrayList<TaskExciseStampDiscrepancies> = ArrayList()

    override fun getExciseStampDiscrepancies(): List<TaskExciseStampDiscrepancies> {
        return stampsDiscrepancies
    }

    override fun findExciseStampsDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskExciseStampDiscrepancies> {
        return findExciseStampsDiscrepanciesOfProduct(product.materialNumber)
    }

    override fun findExciseStampsDiscrepanciesOfProduct(materialNumber: String): List<TaskExciseStampDiscrepancies> {
        return stampsDiscrepancies.filter {stampDiscrepancies ->
            stampDiscrepancies.materialNumber == materialNumber
        }
    }

    override fun addExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        var index = -1
        for (i in stampsDiscrepancies.indices) {
            if (discrepancy.materialNumber == stampsDiscrepancies[i].materialNumber &&
                    discrepancy.code == stampsDiscrepancies[i].code &&
                    discrepancy.typeDiscrepancies == stampsDiscrepancies[i].typeDiscrepancies) {
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
        stampsDiscrepancies.clear()
        newExciseStampDiscrepancies.map {
            addExciseStampDiscrepancy(it)
        }
    }

    override fun changeExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        deleteExciseStampDiscrepancy(discrepancy)
        return addExciseStampDiscrepancy(discrepancy)
    }

    override fun deleteExciseStampDiscrepancy(discrepancy: TaskExciseStampDiscrepancies): Boolean {
        return deleteExciseStampDiscrepancy(discrepancy.materialNumber, discrepancy.code, discrepancy.typeDiscrepancies)
    }

    override fun deleteExciseStampDiscrepancy(materialNumber: String, code: String, typeDiscrepancies: String): Boolean {
        var index = -1
        for (i in stampsDiscrepancies.indices) {
            if (materialNumber == stampsDiscrepancies[i].materialNumber &&
                    code == stampsDiscrepancies[i].code &&
                    typeDiscrepancies == stampsDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        stampsDiscrepancies.removeAt(index)
        return true
    }

    override fun deleteExciseStampsDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskExciseStampDiscrepancies>()
        for (i in stampsDiscrepancies.indices) {
            if (product.materialNumber == stampsDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(stampsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        stampsDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun clear() {
        stampsDiscrepancies.clear()
    }
}