package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class PackInfo(
        /** SAP – код товара */
        @SerializedName("MATNR")
        val material: String,
        /** Код тары */
        @SerializedName("CODE_CONT")
        val code: String,
        /** SAP – код товара */
        @SerializedName("MATNR_OSN")
        val materialOsn: String,
        /** Наименование товара ПФ/ГП */
        @SerializedName("NAME_MATNR_OSN")
        val name: String,
        /** Фактически вес ПФ/ГП */
        @SerializedName("FACT_QNT")
        val quantity: Double
)