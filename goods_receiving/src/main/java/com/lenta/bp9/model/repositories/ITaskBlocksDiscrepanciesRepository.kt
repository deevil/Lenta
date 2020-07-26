package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.*

interface ITaskBlocksDiscrepanciesRepository {
    fun getBlocksDiscrepancies(): List<TaskBlockDiscrepancies>
    fun findBlocksDiscrepanciesOfBlock(block: TaskBlockInfo): List<TaskBlockDiscrepancies>
    fun findBlocksDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskBlockDiscrepancies>
    fun addBlockDiscrepancies(discrepancies: TaskBlockDiscrepancies): Boolean
    fun updateBlocksDiscrepancies(newBlocksDiscrepancies: List<TaskBlockDiscrepancies>)
    fun changeBlockDiscrepancy(discrepancy: TaskBlockDiscrepancies): Boolean
    fun deleteBlockDiscrepancies(discrepancies: TaskBlockDiscrepancies): Boolean
    fun deleteBlockDiscrepancies(materialNumber: String, blockNumber: String, typeDiscrepancies: String): Boolean
    fun deleteBlocksDiscrepanciesForBlock(delBlock: TaskBlockInfo): Boolean
    fun deleteBlocksDiscrepanciesForProduct(product: TaskProductInfo): Boolean
    fun deleteBlocksDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean
    fun deleteBlocksDiscrepanciesNotNormForProduct(materialNumber: String): Boolean
    fun processedNumberOfStampsByProduct(product: TaskProductInfo): Int
    fun notProcessedNumberOfStampsByProduct(product: TaskProductInfo): Double
    fun clear()
}