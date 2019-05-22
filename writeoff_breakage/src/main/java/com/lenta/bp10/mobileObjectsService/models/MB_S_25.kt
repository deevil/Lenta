package com.lenta.bp10.mobileObjectsService.models

import com.google.gson.annotations.SerializedName

// модель справочника штрих-кодов единиц измерения товаров
class MB_S_25(
        /// Глобальный номер товара (GTIN)
        @SerializedName("EAN")
        val ean: String,

        /// Номер товара
        @SerializedName("MATERIAL")
        val material: String,

        /// Единица измерения
        @SerializedName("UOM")
        val uom: String,

        @SerializedName("UMREZ")
        val umrez: Double,

        @SerializedName("UMREN")
        val umren: Double
) {
    fun getUomMultiplier(): Double {
        return umrez / umren
    }
}