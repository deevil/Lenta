package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class ShipmentItem(
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
        /** Номер партии */
        @SerializedName("ZCHARG")
        var shipmentNumber: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var supplierCode: String
)