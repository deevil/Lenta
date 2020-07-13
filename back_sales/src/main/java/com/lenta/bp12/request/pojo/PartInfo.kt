package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class PartInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producerCode: String,
        /** Дата производства */
        @SerializedName("DATEOFPOUR")
        var productionDate: String,
        /** Фактическое количество */
        @SerializedName("PLAN_QNT")
        var planQuantity: String = "",
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        var factQuantity: String,
        /** Единица измерения */
        @SerializedName("BSTME")
        var unitsCode: String,
        /** Номер партии */
        @SerializedName("ZCHARG")
        var partNumber: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var basketNumber: String = ""
)