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
        /** Проблемная марка */
        @SerializedName("IS_MARK_BAD")
        var isBadMark: String,
        /** Код поставщика */
        @SerializedName("LIFNR")
        var producerCode: String
)