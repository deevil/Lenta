package com.lenta.bp14.data.model

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.models.core.Uom


data class Good(
        val id: Int,
        val material: String?,
        val name: String,
        var quantity: Int = 0,
        val uom: Uom = Uom.DEFAULT,
        var priceTagStatus: PriceTagStatus = PriceTagStatus.PRINTED,
        val goodStatus: GoodStatus = GoodStatus.MISSING_RIGHT,
        var salesStatistics: SalesStatistics?,
        val storagePlaces: String,
        val minStock: Int,
        val goodMovement: GoodMovement,
        val priceWithDk: Int,
        val priceWithoutDk: Int,
        var promo: Promo?,

        val comments: MutableList<Comment> = mutableListOf(),
        val shelfLives: MutableList<ShelfLife> = mutableListOf(),
        val deliveries: MutableList<Delivery> = mutableListOf(),
        val providers: MutableList<Provider> = mutableListOf(),
        val stocks: MutableList<Stock> = mutableListOf()
) {

    val quantityField = MutableLiveData<String>(quantity.toString())

    fun getFormattedMaterial(): String? {
        return material?.takeLast(6)
    }

    fun getFormattedMaterialWithName(): String? {
        return getFormattedMaterial() + " " + name
    }

}

enum class PriceTagStatus {
    NO_PRICE_TAG,
    WITH_ERROR,
    PRINTED
}

enum class GoodStatus {
    PRESENT,
    MISSING_WRONG,
    MISSING_RIGHT
}