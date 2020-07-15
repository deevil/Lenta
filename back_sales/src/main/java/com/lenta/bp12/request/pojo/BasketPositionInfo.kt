package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class BasketPositionInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var basketNumber: String,
        /** Количество товаров в корзине */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Максимальная розничная цена */
        @SerializedName("MPR")
        var maxRetailPrice: String = ""
)