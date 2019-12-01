package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskMercuryDiscrepancies
import com.lenta.bp9.model.task.TaskMercuryInfo
import com.lenta.bp9.model.task.TaskProductDiscrepancies
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskMercuryDiscrepanciesRepository {
    fun getMercuryInfo(): List<TaskMercuryInfo>
    fun getMercuryDiscrepancies(): List<TaskMercuryDiscrepancies>
    fun findMercuryInfoOfProduct(product: TaskProductInfo): TaskMercuryInfo?
    fun findMercuryDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskMercuryDiscrepancies>
    fun addMercuryInfo(newMercuryInfo: TaskMercuryInfo): Boolean
    fun addMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun updateMercuryInfo(newMercuryInfo: List<TaskMercuryInfo>)
    fun changeMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepancy(discrepancy: TaskMercuryDiscrepancies): Boolean
    fun deleteMercuryDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun getCountAcceptOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProduct(product: TaskProductInfo): Double
    fun getCountProductNotProcessedOfProduct(product: TaskProductInfo): Double
    fun getCountRefusalOfProductOfReasonRejection(product: TaskProductInfo, reasonRejectionCode: String?): Double
    fun clear()
}