package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class ShipmentInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producer: String,
        /** Дата производства */
        @SerializedName("DATEOFPOUR")
        var productionDate: String,
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        var units: String,
        /** Номер партии */
        @SerializedName("ZCHARG")
        var shipmentNumber: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String
)