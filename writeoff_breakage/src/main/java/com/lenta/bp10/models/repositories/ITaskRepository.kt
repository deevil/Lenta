package com.lenta.bp10.models.repositories

import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.utilities.Logg

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getWriteOffReasons(): ITaskWriteOffReasonRepository
}

fun ITaskRepository.getTotalCountForProduct(productInfo: ProductInfo): Double {
    val arrTaskWriteOffReason = getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo)
    var totalCount = 0.0
    for (i in arrTaskWriteOffReason.indices) {
        totalCount += arrTaskWriteOffReason[i].count

    }
    return totalCount
}