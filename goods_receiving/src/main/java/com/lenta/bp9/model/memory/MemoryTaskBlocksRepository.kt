package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskBlocksRepository
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.utilities.extentions.removeItemFromListWithPredicate

class MemoryTaskBlocksRepository : ITaskBlocksRepository {

    private val blocks: ArrayList<TaskBlockInfo> = ArrayList()

    override fun getBlocks(): List<TaskBlockInfo> {
        return blocks
    }

    override fun findBlock(block: TaskBlockInfo): TaskBlockInfo? {
        return blocks.firstOrNull { it.blockNumber == block.blockNumber}
    }

    override fun findBlocksOfProduct(productInfo: TaskProductInfo): List<TaskBlockInfo>? {
        return blocks.filter { it.materialNumber == productInfo.materialNumber}
    }

    override fun addBlock(block: TaskBlockInfo): Boolean {
        var index = -1
        for (i in blocks.indices) {
            if (block.blockNumber == blocks[i].blockNumber) {
                index = i
            }
        }

        if (index == -1) {
            blocks.add(block)
            return true
        }
        return false
    }

    override fun updateBlocks(newBlocks: List<TaskBlockInfo>) {
        clear()
        newBlocks.map {
            addBlock(it)
        }
    }

    override fun changeBlock(block: TaskBlockInfo): Boolean {
        deleteBlock(block)
        return addBlock(block)
    }

    override fun deleteBlock(block: TaskBlockInfo): Boolean {
        return blocks.removeItemFromListWithPredicate {blockInfo ->
            blockInfo.blockNumber == block.blockNumber
        }
    }

    override fun deleteBlocks(delBlocks: List<TaskBlockInfo>): Boolean {
        return blocks.removeAll(delBlocks)
    }

    override fun deleteBlocksForProduct(product: TaskProductInfo): Boolean {
        return blocks.removeItemFromListWithPredicate {blockInfo ->
            blockInfo.materialNumber == product.materialNumber
        }
    }

    override fun clear() {
        blocks.clear()
    }
}