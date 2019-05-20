package com.lenta.bp10.models.repositories

import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.models.core.ProductInfo

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getWriteOffReasons(): ITaskWriteOffReasonRepository
}

fun ITaskRepository.getTotalCountForProduct(productInfo: ProductInfo, writeOffReason: WriteOffReason? = null): Double {
    val arrTaskWriteOffReason = getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo)
    var totalCount = 0.0
    arrTaskWriteOffReason.forEach {
        if (writeOffReason == null || writeOffReason.code == it.writeOffReason.code) {
            totalCount += it.count
        }

    }
    return totalCount
}