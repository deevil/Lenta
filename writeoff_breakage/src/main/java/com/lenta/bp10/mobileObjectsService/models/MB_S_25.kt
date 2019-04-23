package com.lenta.bp10.mobileObjectsService.models

import com.fasterxml.jackson.annotation.JsonProperty

// модель справочника штрих-кодов единиц измерения товаров
class MB_S_25(
        /// Глобальный номер товара (GTIN)
        @JsonProperty("EAN")
        val ean: String,

        /// Номер товара
        @JsonProperty("MATERIAL")
        val material: String,

        /// Единица измерения
        @JsonProperty("UOM")
        val uom: String,

        @JsonProperty("UMREZ")
        val umrez: Double,

        @JsonProperty("UMREN")
        val umren: Double
) {
    fun getUomMultiplier(): Double {
        return umrez / umren
    }
}