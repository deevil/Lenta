package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskMercuryDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskMercuryInfo
import com.lenta.bp9.model.task.TaskProductInfo

class MemoryTaskMercuryDiscrepanciesRepository : ITaskMercuryDiscrepanciesRepository {

    private val mercuryInfo: ArrayList<TaskMercuryInfo> = ArrayList()
    private val mercuryDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()

    override fun getMercuryInfo(): List<TaskMercuryInfo> {
        return mercuryInfo
    }

    override fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies> {
        return mercuryDiscrepancies
    }

    override fun findMercuryInfoOfProduct(product: TaskProductInfo): TaskMercuryInfo? {
        return mercuryInfo.firstOrNull { it.materialNumber == product.materialNumber}
    }

    override fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskMercuryDiscrepancies>()
        for (i in mercuryDiscrepancies.indices) {
            if (product.materialNumber == mercuryDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(mercuryDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addMercuryInfo(newMercuryInfo: TaskMercuryInfo): Boolean {
        var index = -1
        for (i in mercuryDiscrepancies.indices) {
            if (newMercuryInfo.materialNumber == mercuryInfo[i].materialNumber) {
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
            if (discrepancy.materialNumber == mercuryDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == mercuryDiscrepancies[i].typeDiscrepancies) {
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
        for (mercuryInfo in newMercuryInfo) {
            addMercuryInfo(mercuryInfo)
        }
    }

    override fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        deleteMercuryDiscrepancy(discrepancy)
        return addMercuryDiscrepancy(discrepancy)
    }

    override fun deleteMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        var index = -1
        for (i in mercuryDiscrepancies.indices) {
            if (discrepancy.materialNumber == mercuryDiscrepancies[i].materialNumber && discrepancy.typeDiscrepancies == mercuryDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        mercuryDiscrepancies.removeAt(index)
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

        mercuryDiscrepancies.removeAll(delDiscrepancies)
        return true
    }

    override fun getCountAcceptOfProduct(product: TaskProductInfo): Double {
        var countAccept = 0.0
        findMercuryDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies == "1"
        }.map {discrepancies ->
            countAccept += discrepancies.numberDiscrepancies.toDouble()
        }
        return countAccept
    }

    override fun getCountRefusalOfProduct(product: TaskProductInfo): Double {
        var countRefusal = 0.0
        findMercuryDiscrepanciesOfProduct(product).filter {
            it.typeDiscrepancies != "1"
        }.map {discrepancies ->
            countRefusal += discrepancies.numberDiscrepancies.toDouble()
        }
        return countRefusal
    }

    override fun getCountProductNotProcessedOfProduct(product: TaskProductInfo): Double {
        return product.origQuantity.toDouble() - getCountAcceptOfProduct(product) - getCountRefusalOfProduct(product)
    }

    override fun getCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double {
        var countRefusal = 0.0
        reasonRejectionCode?.let {
            findMercuryDiscrepanciesOfProduct(product).filter {
                it.typeDiscrepancies == reasonRejectionCode
            }.map {discrepancies ->
                countRefusal += discrepancies.numberDiscrepancies.toDouble()
            }
        }
        return countRefusal
    }


    override fun clear() {
        mercuryDiscrepancies.clear()
    }
}