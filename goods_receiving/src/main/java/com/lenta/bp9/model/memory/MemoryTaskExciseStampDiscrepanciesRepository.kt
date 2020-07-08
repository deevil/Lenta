package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskExciseStampDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants

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
        return deleteExciseStampDiscrepancy(discrepancy.code)
    }

    override fun deleteExciseStampDiscrepancy(exciseStampCode: String): Boolean {
        stampsDiscrepancies.map { it }.filter { discrepancies ->
            if (exciseStampCode == discrepancies.code) {
                stampsDiscrepancies.remove(discrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
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

        delDiscrepancies.map {
            deleteExciseStampDiscrepancy(it)
        }
        return true
    }

    override fun deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        val delDiscrepancies = ArrayList<TaskExciseStampDiscrepancies>()
        for (i in stampsDiscrepancies.indices) {
            if (stampsDiscrepancies[i].materialNumber == materialNumber &&
                    stampsDiscrepancies[i].typeDiscrepancies == typeDiscrepancies) {
                delDiscrepancies.add(stampsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteExciseStampDiscrepancy(it)
        }
        return true
    }

    override fun deleteExciseStampsDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        val delDiscrepancies = ArrayList<TaskExciseStampDiscrepancies>()
        for (i in stampsDiscrepancies.indices) {
            if (stampsDiscrepancies[i].materialNumber == materialNumber &&
                    stampsDiscrepancies[i].typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                delDiscrepancies.add(stampsDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteExciseStampDiscrepancy(it)
        }
        return true
    }

    override fun deleteExciseStampDiscrepancyOfProductOfBoxOfDiscrepancy(materialNumber: String, boxNumber: String, typeDiscrepancies: String): Boolean {
        stampsDiscrepancies.map { it }.filter { discrepancies ->
            if (materialNumber == discrepancies.materialNumber && boxNumber == discrepancies.boxNumber && typeDiscrepancies == discrepancies.typeDiscrepancies) {
                stampsDiscrepancies.remove(discrepancies)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        stampsDiscrepancies.clear()
    }
}