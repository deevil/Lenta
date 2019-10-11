package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class Mark(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val matNr: String,
        /** Номер марки */
        @SerializedName("MARK_NUM")
        val markNumber: String
)