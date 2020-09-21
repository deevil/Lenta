package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * ET_PROD_TEXT
 * @see com.lenta.bp12.request.ScanInfoNetRequest
 * */
data class ProducerInfo(
        /** Номер счета */
        @SerializedName("ZPROD")
        val code: String? = "",
        /** Наименование поставщика */
        @SerializedName("PROD_NAME")
        val name: String? = ""
)