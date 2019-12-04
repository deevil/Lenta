package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class RawInfo(
        /** Номер технологического заказа */
        @SerializedName("AUFNR")
        val orderNumber: String,
        /** SAP – код товара сырье */
        @SerializedName("MATNR")
        val material: String,
        /** SAP – код товара */
        @SerializedName("MATNR_OSN")
        val materialOsn: String,
        /** Наименование товара ПФ/ГП */
        @SerializedName("NAME_MATNR_OSN")
        val name: String,
        /** Плановое количество сырья */
        @SerializedName("PLAN_QNT")
        val planned: Double
)