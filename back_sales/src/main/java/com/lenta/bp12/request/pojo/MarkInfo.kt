package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

data class MarkInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Код акцизной марки */
        @SerializedName("MARK_NUM")
        var number: String,
        /** Номер коробки */
        @SerializedName("BOX_NUM")
        var boxNumber: String,
        /** Номер блока */
        @SerializedName("PACK_NUM")
        var packNumber: String = "",
        /** Проблемная марка */
        @SerializedName("IS_MARK_BAD")
        var isBadMark: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var producerCode: String,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var basketNumber: String = "",
        /** Максимальная розничная цена */
        @SerializedName("MPR")
        var maxRetailPrice: String = ""
)