package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskMercuryDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskMercuryInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.models.core.Uom

class MemoryTaskMercuryDiscrepanciesRepository : ITaskMercuryDiscrepanciesRepository {

    private val mercuryInfo: ArrayList<TaskMercuryInfo> = ArrayList()
    private val mercuryDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()

    override fun getMercuryInfo(): List<TaskMercuryInfo> {
        return mercuryInfo
    }

    override fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies> {
        return mercuryDiscrepancies
    }

    override fun findMercuryInfoOfProduct(product: TaskProductInfo): List<TaskMercuryInfo> {
        return mercuryInfo.filter { it.materialNumber == product.materialNumber}
    }

    override fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies> {
        return mercuryDiscrepancies.filter { it.materialNumber == product.materialNumber}
    }

    override fun addMercuryInfo(newMercuryInfo: TaskMercuryInfo): Boolean {
        var index = -1
        for (i in mercuryInfo.indices) {
            if (newMercuryInfo.vetDocumentID == mercuryInfo[i].vetDocumentID && newMercuryInfo.typeDiscrepancies == mercuryInfo[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            mercuryInfo.add(newMercuryInfo)
            return true
        }
        return false
    }

    override fun addMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        var index = -1
        for (i in mercuryDiscrepancies.indices) {
            if (discrepancy.materialNumber == mercuryDiscrepancies[i].materialNumber &&
                    discrepancy.manufacturer == mercuryDiscrepancies[i].manufacturer &&
                    discrepancy.productionDate == mercuryDiscrepancies[i].productionDate &&
                    discrepancy.vetDocumentID == mercuryDiscrepancies[i].vetDocumentID &&
                    discrepancy.typeDiscrepancies == mercuryDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            mercuryDiscrepancies.add(discrepancy)
            return true
        }
        return false
    }

    override fun updateMercuryInfo(newMercuryInfo: List<TaskMercuryInfo>) {
        mercuryInfo.clear()
        newMercuryInfo.map {
            addMercuryInfo(it)
        }
    }

    override fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        deleteMercuryDiscrepancy(discrepancy)
        return addMercuryDiscrepancy(discrepancy)
    }

    override fun deleteMercuryDiscrepancy(delDiscrepancy: TaskMercuryDiscrepancies): Boolean {
        mercuryDiscrepancies.map { it }.filter {discrepancy ->
            if (delDiscrepancy.materialNumber == discrepancy.materialNumber &&
                    delDiscrepancy.manufacturer == discrepancy.manufacturer &&
                    delDiscrepancy.productionDate == discrepancy.productionDate &&
                    delDiscrepancy.vetDocumentID == discrepancy.vetDocumentID &&
                    delDiscrepancy.typeDiscrepancies == discrepancy.typeDiscrepancies) {
                mercuryDiscrepancies.remove(discrepancy)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteMercuryDiscrepancyOfProduct(materialNumber: String, typeDiscrepancies: String): Boolean {
        val delDiscrepancies = ArrayList<TaskMercuryDiscrepancies>()
        for (i in mercuryDiscrepancies.indices) {
            if (materialNumber == mercuryDiscrepancies[i].materialNumber && typeDiscrepancies == mercuryDiscrepancies[i].typeDiscrepancies) {
                delDiscrepancies.add(mercuryDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        mercuryDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun deleteMercuryDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        val delDiscrepancies = ArrayList<TaskMercuryDiscrepancies>()
        for (i in mercuryDiscrepancies.indices) {
            if (product.materialNumber == mercuryDiscrepancies[i].materialNumber) {
                delDiscrepancies.add(mercuryDiscrepancies[i])
            }
        }

        if (delDiscrepancies.isEmpty()) {
            return false
        }

        delDiscrepancies.map {
            deleteMercuryDiscrepancy(it)
        }
        return true
    }

    override fun getMercuryCountAcceptOfProduct(product: TaskProductInfo): Double {
        var countAccept = 0.0
        findMercuryDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies == "1"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies
        }
        return countAccept
    }

    override fun getMercuryCountRefusalOfProduct(product: TaskProductInfo): Double {
        var countRefusal = 0.0
        findMercuryDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies != "1"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies
        }
        return countRefusal
    }

    override fun getMercuryCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double {
        var countRefusal = 0.0
        reasonRejectionCode?.let {
            findMercuryDiscrepanciesOfProduct(product).filter {
                it.typeDiscrepancies == reasonRejectionCode
            }.map {discrepancies ->
                countRefusal += discrepancies.numberDiscrepancies
            }
        }
        return countRefusal
    }

    override fun getManufacturesOfProduct(product: TaskProductInfo) : List<String> {
        return findMercuryInfoOfProduct(product).groupBy {
            it.manufacturer
        }.map {
            it.key
        }
    }

    override fun clear() {
        mercuryInfo.clear()
        mercuryDiscrepancies.clear()
    }
}