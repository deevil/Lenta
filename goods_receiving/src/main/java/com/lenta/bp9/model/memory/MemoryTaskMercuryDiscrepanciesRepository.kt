package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskMercuryDiscrepanciesRepository
import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants

class MemoryTaskMercuryDiscrepanciesRepository : ITaskMercuryDiscrepanciesRepository {

    private val mercuryDiscrepancies: ArrayList<TaskMercuryDiscrepancies> = ArrayList()

    override fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies> {
        return mercuryDiscrepancies
    }

    override fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies> {
        return findMercuryDiscrepanciesOfProduct(product.materialNumber)
    }

    override fun findMercuryDiscrepanciesOfProduct(materialNumber: String): List<TaskMercuryDiscrepancies> {
        return mercuryDiscrepancies.filter { it.materialNumber == materialNumber}
    }

    override fun addMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        var index = -1
        for (i in mercuryDiscrepancies.indices) {
            if (discrepancy.materialNumber == mercuryDiscrepancies[i].materialNumber &&
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

    override fun updateMercuryDiscrepancy(newMercuryDiscrepancy: List<TaskMercuryDiscrepancies>) {
        clear()
        newMercuryDiscrepancy.map { addMercuryDiscrepancy(it) }
    }

    override fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean {
        deleteMercuryDiscrepancy(discrepancy)
        return addMercuryDiscrepancy(discrepancy)
    }

    override fun deleteMercuryDiscrepancy(delDiscrepancy: TaskMercuryDiscrepancies): Boolean {
        mercuryDiscrepancies.map { it }.filter {discrepancy ->
            if (delDiscrepancy.materialNumber == discrepancy.materialNumber &&
                    delDiscrepancy.vetDocumentID == discrepancy.vetDocumentID &&
                    (delDiscrepancy.typeDiscrepancies == discrepancy.typeDiscrepancies
                            || discrepancy.typeDiscrepancies.isEmpty())) {
                mercuryDiscrepancies.remove(discrepancy)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    private fun changeGroupByMercuryDiscrepancies(delDiscrepancy: TaskMercuryDiscrepancies) {
        deleteMercuryDiscrepancy(delDiscrepancy)
        addMercuryDiscrepancy(delDiscrepancy.copy(typeDiscrepancies = "", numberDiscrepancies = 0.0))
    }

    override fun deleteMercuryDiscrepancyOfProduct(materialNumber: String, typeDiscrepancies: String) {
        findMercuryDiscrepanciesOfProduct(materialNumber)
                .asSequence()
                .groupBy {
                    it.typeDiscrepancies == typeDiscrepancies
                }
                .map { groupByMercuryDiscrepancies ->
                    groupByMercuryDiscrepancies.value.map { changeGroupByMercuryDiscrepancies(it)}
                }
                .toList()
    }

    override fun deleteMercuryDiscrepanciesForProduct(product: TaskProductInfo) {
        findMercuryDiscrepanciesOfProduct(product.materialNumber)
                .asSequence()
                .groupBy { it.vetDocumentID }
                .map { groupByMercuryDiscrepancies ->
                    groupByMercuryDiscrepancies.value.map { changeGroupByMercuryDiscrepancies(it)}
                }
                .toList()
    }

    override fun deleteMercuryDiscrepanciesNotNormForProduct(product: TaskProductInfo) {
        findMercuryDiscrepanciesOfProduct(product.materialNumber)
                .asSequence()
                .filter { it.typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                .groupBy { it.vetDocumentID }
                .map { groupByMercuryDiscrepancies ->
                    groupByMercuryDiscrepancies.value.map { changeGroupByMercuryDiscrepancies(it)}
                }
                .toList()
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
        return findMercuryDiscrepanciesOfProduct(product)
                .groupBy { it.manufacturer }
                .map { it.key }
    }

    override fun clear() {
        mercuryDiscrepancies.clear()
    }
}