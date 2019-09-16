package com.lenta.bp14.models.data.pojo

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.models.core.Uom

data class Good(
        val id: Int,
        val material: String?,
        val ean: String,
        val name: String,
        var quantity: Int = 0,
        val uom: Uom = Uom.DEFAULT,
        var printStatus: PrintStatus = PrintStatus.PRINTED,
        val priceTagStatus: PriceTagStatus = PriceTagStatus.MISSING,
        var salesStatistics: SalesStatistics?,
        val shelfLifeDays: Int,
        val storagePlaces: String,
        val minStock: Int,
        val goodMovement: GoodMovement,
        val price: Price,
        var promo: Promo?,
        var goodGroup: String,
        var purchaseGroup: String,
        val firstStorageStock: Int,

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

enum class PrintStatus {
    NOT_PRINTED,
    PRINTED
}

enum class PriceTagStatus {
    CORRECT,
    WITH_ERROR,
    MISSING
}