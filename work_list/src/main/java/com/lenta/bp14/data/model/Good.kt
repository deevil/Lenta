package com.lenta.bp14.data.model

import com.lenta.shared.models.core.Uom


data class Good(
        val id: Int,
        val material: String?,
        val name: String,
        val total: Int = 0,
        val uom: Uom = Uom.DEFAULT,
        var goodStatus: GoodStatus = GoodStatus.MISSING,
        val priceTagStatus: PriceTagStatus = PriceTagStatus.PRINTED
) {

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

}

enum class GoodStatus {
    MISSING,
    ERROR
}

enum class PriceTagStatus {
    PRINTED,
    MISSING
}