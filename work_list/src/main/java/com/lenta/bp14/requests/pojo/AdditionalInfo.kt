package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName


data class AdditionalInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matnr: String,
        /** Постоянный не снижаемый запас */
        @SerializedName("MIN_STOCK")
        val minStock: String,
        /** Данные последней инвентаризации */
        @SerializedName("LAST_INV")
        val lastInv: String,
        /** Данные ближайшей поставки */
        @SerializedName("PLAN_DELIVERY")
        val planDelivery: String,
        /** Цена без ДК */
        @SerializedName("PRICE1")
        val price1: String,
        /** Цена с ДК */
        @SerializedName("PRICE2")
        val price2: String,
        /** Данные ближайшей промо-акции (текст первой строки) */
        @SerializedName("PROMO_TEXT1")
        val promoText1: String,
        /** Данные ближайшей промо-акции (текст второй строки) */
        @SerializedName("PROMO_TEXT2")
        val promoText2: String
)