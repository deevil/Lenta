package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface ITaskBlocksRepository {
    fun getBlocks(): List<TaskBlockInfo>
    fun findBlock(block: TaskBlockInfo): TaskBlockInfo?
    fun findBlocksOfProduct(productInfo: TaskProductInfo): List<TaskBlockInfo>?
    fun addBlock(block: TaskBlockInfo): Boolean
    fun updateBlocks(newBlocks: List<TaskBlockInfo>)
    fun changeBlock(block: TaskBlockInfo): Boolean
    fun deleteBlock(block: TaskBlockInfo): Boolean
    fun deleteBlocks(delBlocks: List<TaskBlockInfo>): Boolean
    fun deleteBlocksForProduct(product: TaskProductInfo): Boolean
    fun clear()
}