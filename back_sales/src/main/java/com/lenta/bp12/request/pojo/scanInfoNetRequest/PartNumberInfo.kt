package com.lenta.bp12.request.pojo.scanInfoNetRequest

import com.google.gson.annotations.SerializedName

/**
 * ET_ZCHARG
 * @see com.lenta.bp12.request.ScanInfoNetRequest
 * */
data class PartNumberInfo(
        /** Номер партии */
        @SerializedName("ZCHARG")
        val partNumber: String? = "",
        /** Фактическое количество */
        @SerializedName("CHARG_QNT")
        val quantity: String? = "",
        /** Фактическое количество */
        @SerializedName("MEINS")
        val uom: String? = ""
)