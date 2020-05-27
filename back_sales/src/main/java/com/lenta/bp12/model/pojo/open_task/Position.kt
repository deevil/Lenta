package com.lenta.bp12.model.pojo.open_task

import com.lenta.bp12.model.Category
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.Uom

data class Position(
        var quantity: Double = 0.0,
        val innerQuantity: Double = 1.0,
        val units: Uom = Uom.ST,
        var category: Category = Category.QUANTITY,
        var provider: ProviderInfo,
        var date: String? = null,
        var isCounted: Boolean = false,
        var isDelete: Boolean = false
) {

    fun isBox(): Boolean {
        return innerQuantity > 1 // По умолчанию всегда 1.0
    }

}