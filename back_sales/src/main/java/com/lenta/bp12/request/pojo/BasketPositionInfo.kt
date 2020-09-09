package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class BasketPositionInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val material: String?,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String?,
        /** Количество товаров в корзине */
        @SerializedName("FACT_QNT")
        val quantity: String?,
        /** Максимальная розничная цена */
        @SerializedName("MPR")
        val maxRetailPrice: String? = ""
)