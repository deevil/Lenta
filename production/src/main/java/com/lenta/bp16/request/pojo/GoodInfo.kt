package com.lenta.bp16.request.pojo

import com.google.gson.annotations.SerializedName

data class GoodInfo(
        /** Номер заказа */
        @SerializedName("EBELN")
        val number: String,
        /** SAP – код товара */
        @SerializedName("MATNR")
        val material: String,
        /** Наименование товара */
        @SerializedName("NAME_MATNR")
        val name: String,
        /** Единица измерения товара */
        @SerializedName("BUOM")
        val unitsCode: String,
        /** Количество поступившего сырья */
        @SerializedName("RAW_QNT")
        val quantity: Double
)