package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Position(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matNr: String,
        /** Индикатор: Позиция посчитана */
        @SerializedName("XZAEL")
        val isProcessed: String,
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        val quantity: Double
)


