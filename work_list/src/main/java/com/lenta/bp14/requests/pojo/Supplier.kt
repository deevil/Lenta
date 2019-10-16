package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Supplier(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matnr: String,
        /** SAP-код поставщика */
        @SerializedName("LIFNR")
        val lifnr: String,
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        val lifnrName: String,
        /** Период актуальности поставщика */
        @SerializedName("PERIOD_ACT")
        val periodAct: String
)