package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class GoodItem(
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
        /** Количество вложенного */
        @SerializedName("QNTINCL")
        var innerQuantity: String,
        /** Единицы измерения заказа */
        @SerializedName("BSTME")
        var unitsCode: String
)