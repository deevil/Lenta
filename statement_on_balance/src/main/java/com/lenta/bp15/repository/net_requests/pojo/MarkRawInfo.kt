package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName

data class MarkRawInfo(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val material: String,
        /** Код идентификации */
        @SerializedName("MARK_NUM")
        val number: String,
        /** Признак: марка обработана */
        @SerializedName("IS_SCAN")
        val isScan: String
)