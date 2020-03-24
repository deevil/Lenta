package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class PartInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producer: String,
        /** Дата производства */
        @SerializedName("DATEOFPOUR")
        var productionDate: String,
        /** Фактическое количество в ЕИЗ */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        var unitsCode: String,
        /** Номер партии */
        @SerializedName("ZCHARG")
        var partNumber: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String
)