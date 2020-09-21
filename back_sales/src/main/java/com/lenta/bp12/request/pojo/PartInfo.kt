package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * @see com.lenta.bp12.request.SendTaskDataNetRequest
 * IT_TASK_PARTS
 * */
data class PartInfo(
        /** SAP-код родителя */
        @SerializedName("MATNR_OSN")
        var materialOsn: String = "",
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Производитель товара */
        @SerializedName("ZPROD")
        var producerCode: String,
        /** Дата производства */
        @SerializedName("DATEOFPOUR")
        var productionDate: String,
        @SerializedName("PLAN_QNT")
        var plannedQuantity: String? = "",
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        var quantity: String,
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