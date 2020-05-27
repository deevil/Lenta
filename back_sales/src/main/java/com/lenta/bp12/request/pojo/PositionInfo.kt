package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class PositionInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String,
        /** Наименование поставщика */
        @SerializedName("LIFNR_NAME")
        var providerName: String,
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Индикатор: Позиция посчитана */
        @SerializedName("XZAEL")
        var isCounted: String,
        /** Позиция удалена */
        @SerializedName("IS_DEL")
        var isDeleted: String,
        /** Вложенное количество */
        @SerializedName("QNTINCL")
        var innerQuantity: String = "",
        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        var unitsCode: String = ""
)