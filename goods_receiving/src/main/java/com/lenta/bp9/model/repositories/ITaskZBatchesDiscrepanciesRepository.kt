package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.PartySignsOfZBatches
import com.lenta.bp9.model.task.TaskBatchInfo
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepancies

interface ITaskZBatchesDiscrepanciesRepository {
    fun getZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies>
    fun findZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): List<TaskZBatchesDiscrepancies>
    fun findZBatchDiscrepanciesOfProduct(materialNumber: String): List<TaskZBatchesDiscrepancies>
    fun addZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean
    fun updateZBatchesDiscrepancy(newZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>)
    fun changeZBatchDiscrepancy(discrepancy: TaskZBatchesDiscrepancies): Boolean
    fun deleteZBatchDiscrepancies(discrepancies: TaskZBatchesDiscrepancies): Boolean
    fun deleteZBatchesDiscrepanciesForProduct(materialNumber: String): Boolean
    fun deleteZBatchesDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean
    fun getCountAcceptOfZBatch(discrepancies: TaskZBatchesDiscrepancies): Double
    fun getCountAcceptOfZBatchPGE(discrepancies: TaskZBatchesDiscrepancies): Double
    fun getCountRefusalOfZBatchPGE(discrepancies: TaskZBatchesDiscrepancies): Double
    fun findPartySignOfZBatch(zBatchesDiscrepancies: TaskZBatchesDiscrepancies): PartySignsOfZBatches?
    fun findPartySignsOfProduct(materialNumber: String, processingUnit: String): List<PartySignsOfZBatches>
    fun addPartySignOfZBatches(partySign: PartySignsOfZBatches): Boolean
    fun changePartySign(partySign: PartySignsOfZBatches): Boolean
    fun deletePartySignOfZBatches(partySign: PartySignsOfZBatches): Boolean
    fun clear()
}