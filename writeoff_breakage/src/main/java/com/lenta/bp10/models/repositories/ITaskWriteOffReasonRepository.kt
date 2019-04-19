package com.lenta.bp10.models.repositories

import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.shared.models.core.ProductInfo

interface ITaskWriteOffReasonRepository {
    fun getWriteOffReasons(): List<TaskWriteOffReason>
    fun findWriteOffReasonsOfProduct(product: ProductInfo): List<TaskWriteOffReason>
    fun addWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean
    fun deleteWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean
    fun deleteWriteOffReasonsForProduct(product: ProductInfo): Boolean
    fun clear()
    fun get(index: Int): TaskWriteOffReason
    fun lenght(): Int
}