package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBlocksDiscrepanciesRepository
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskBlocksDiscrepanciesRepository : ITaskBlocksDiscrepanciesRepository {

    private val blocksDiscrepancies: ArrayList<TaskBlockDiscrepancies> = ArrayList()

    override fun getBlocksDiscrepancies(): List<TaskBlockDiscrepancies> {
        return blocksDiscrepancies
    }

    override fun findBlocksDiscrepanciesOfBlock(block: TaskBlockInfo): List<TaskBlockDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBlockDiscrepancies>()
        for (i in blocksDiscrepancies.indices) {
            if (block.blockNumber == blocksDiscrepancies[i].blockNumber) {
                foundDiscrepancies.add(blocksDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun findBlocksDiscrepanciesOfProduct(product: TaskProductInfo): List<TaskBlockDiscrepancies> {
        val foundDiscrepancies = ArrayList<TaskBlockDiscrepancies>()
        for (i in blocksDiscrepancies.indices) {
            if (product.materialNumber == blocksDiscrepancies[i].materialNumber) {
                foundDiscrepancies.add(blocksDiscrepancies[i])
            }
        }
        return foundDiscrepancies
    }

    override fun addBlockDiscrepancies(discrepancies: TaskBlockDiscrepancies): Boolean {
        var index = -1
        for (i in blocksDiscrepancies.indices) {
            if (discrepancies.materialNumber == blocksDiscrepancies[i].materialNumber &&
                    discrepancies.blockNumber == blocksDiscrepancies[i].blockNumber &&
                    discrepancies.typeDiscrepancies == blocksDiscrepancies[i].typeDiscrepancies) {
                index = i
            }
        }

        if (index == -1) {
            blocksDiscrepancies.add(discrepancies)
            return true
        }
        return false
    }

    override fun updateBlocksDiscrepancies(newBlocksDiscrepancies: List<TaskBlockDiscrepancies>) {
        clear()
        newBlocksDiscrepancies.map {
            addBlockDiscrepancies(it)
        }
    }

    override fun changeBlockDiscrepancy(discrepancy: TaskBlockDiscrepancies): Boolean {
        deleteBlockDiscrepancies(discrepancy)
        return addBlockDiscrepancies(discrepancy)
    }

    override fun deleteBlockDiscrepancies(discrepancies: TaskBlockDiscrepancies): Boolean {
        return deleteBlockDiscrepancies(discrepancies.materialNumber, discrepancies.blockNumber, discrepancies.typeDiscrepancies)
    }

    override fun deleteBlockDiscrepancies(materialNumber: String, blockNumber: String, typeDiscrepancies: String): Boolean {
        return blocksDiscrepancies.removeItemFromListWithPredicate { block ->
            materialNumber == block.materialNumber
                    && blockNumber == block.blockNumber
                    && typeDiscrepancies == block.typeDiscrepancies
        }
    }

    override fun deleteBlocksDiscrepanciesForBlock(delBlock: TaskBlockInfo): Boolean {
        return blocksDiscrepancies.removeItemFromListWithPredicate { block ->
            delBlock.materialNumber == block.materialNumber &&
                    delBlock.blockNumber == block.blockNumber
        }
    }

    override fun deleteBlocksDiscrepanciesForProduct(product: TaskProductInfo): Boolean {
        return blocksDiscrepancies.removeItemFromListWithPredicate { block ->
            block.materialNumber == product.materialNumber
        }
    }

    override fun deleteBlocksDiscrepanciesForProductAndDiscrepancies(materialNumber: String, typeDiscrepancies: String): Boolean {
        return blocksDiscrepancies.removeItemFromListWithPredicate { block ->
            materialNumber == block.materialNumber &&
                    typeDiscrepancies == block.typeDiscrepancies
        }
    }

    override fun deleteBlocksDiscrepanciesNotNormForProduct(materialNumber: String): Boolean {
        return blocksDiscrepancies.removeItemFromListWithPredicate { block ->
            block.materialNumber == materialNumber &&
                    block.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    override fun processedNumberOfStampsByProduct(product: TaskProductInfo): Int {
        return findBlocksDiscrepanciesOfProduct(product)
                .filter {
                    it.isScan && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }.size
    }

    override fun notProcessedNumberOfStampsByProduct(product: TaskProductInfo): Double {
        val processedNumberOfStampsByProduct = findBlocksDiscrepanciesOfProduct(product)
                .filter {
                    it.isScan
                }
                .size
                .toDouble()
        val origQuantity = product.origQuantity.toDouble()
        return origQuantity - processedNumberOfStampsByProduct
    }

    override fun clear() {
        blocksDiscrepancies.clear()
    }
}