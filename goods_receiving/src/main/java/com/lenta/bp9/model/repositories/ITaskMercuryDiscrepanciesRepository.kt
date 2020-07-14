package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskMercuryInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskMercuryDiscrepanciesRepository {
    fun getMercuryInfo(): List<TaskMercuryInfo>
    fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies>
    fun findMercuryInfoOfProduct(product: TaskProductInfo): List<TaskMercuryInfo>
    fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies>
    fun addMercuryInfo(newMercuryInfo: TaskMercuryInfo): Boolean
    fun addMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun updateMercuryInfo(newMercuryInfo: List<TaskMercuryInfo>)
    fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepancyOfProduct(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteMercuryDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun deleteMercuryDiscrepanciesNotNormForProduct(product: TaskProductInfo): Boolean
    fun getMercuryCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getMercuryCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getMercuryCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double
    fun getManufacturesOfProduct(product: TaskProductInfo) : List<String>
    fun clear()
}