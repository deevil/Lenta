package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class CheckResult(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matNr: String,
        /** Фактическое кол-во */
        @SerializedName("FACT_QNT")
        val quantity: Double,
        /** Комментарий (код) */
        @SerializedName("COMMENT")
        val commentCode: String,
        /** Дата производства */
        @SerializedName("DATA_PROD")
        val producedDate: String,
        /** Срок годности */
        @SerializedName("SHELF_LIFE")
        val shelfLife: String
)