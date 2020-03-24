package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class GoodInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var providerCode: String,
        /** Фактическое количество */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Индикатор: Позиция посчитана */
        @SerializedName("XZAEL")
        var isCounted: String,
        /** Позиция удалена */
        @SerializedName("IS_DEL")
        var isDeleted: String,
        /** Единица измерения заказа на поставку */
        @SerializedName("BSTME")
        var unitsCode: String
)