package com.lenta.bp16.model.ingredients.ui

import com.google.gson.annotations.SerializedName

/**
 * Данные ШК по товарам
 */
data class OrderByBarcode(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matnr: String,

        /** Глобальный номер товара (GTIN) */
        @SerializedName("EAN")
        val ean: String,

        /** Единица измерения */
        @SerializedName("EAN_UOM")
        val ean_nom: String,

        /** Числитель для пересчета в базисные единицы измерения */
        @SerializedName("EAN_UMREZ")
        val ean_umrez: String,

        /** Знаменатель при пересчете в базисные единицы измерения */
        @SerializedName("EAN_UMREN")
        val ean_umren: String
)