package com.lenta.bp18.request.pojo

import com.google.gson.annotations.SerializedName

data class GoodInfo(
        /** SAP – код товара сырье */
        @SerializedName("MATNR")
        val material: String?,
        /** Наименование товара сырье\ПФ */
        @SerializedName("NAME_MATNR")
        val name: String?,
        /** Единица измерения товара */
        @SerializedName("BUOM")
        val unitsCode: String?,
        /** Количество поступившего сырья */
        @SerializedName("RAW_QNT")
        val quantity: Double?
)