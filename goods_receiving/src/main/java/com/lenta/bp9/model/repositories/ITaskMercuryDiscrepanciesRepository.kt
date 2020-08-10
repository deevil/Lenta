package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskMercuryDiscrepanciesRepository {
    fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies>
    fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies>
    fun findMercuryDiscrepanciesOfProduct(materialNumber: String): List<TaskMercuryDiscrepancies>
    fun addMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun updateMercuryDiscrepancy(newMercuryDiscrepancy: List<TaskMercuryDiscrepancies>)
    fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepancyOfProduct(materialNumber: String, typeDiscrepancies: String)
    fun deleteMercuryDiscrepanciesForProduct(product: TaskProductInfo)
    fun deleteMercuryDiscrepanciesNotNormForProduct(product: TaskProductInfo)
    fun getMercuryCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getMercuryCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getMercuryCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double
    fun getManufacturesOfProduct(product: TaskProductInfo) : List<String>
    fun clear()
}