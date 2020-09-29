package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

/**
 * Структура: ZSWKL_ZPARTS_EXCH, z-партия
 */
data class ZPartDTO(
        /** Внутренняя z-партия */
        @SerializedName("BATCH")
        val batch: String?,
        /** Склад */
        @SerializedName("LGORT")
        val stock: String?,
        /** SAP-Производитель партии */
        @SerializedName("PRODUCER")
        val producer: String?,
        /** Количество */
        @SerializedName("QUANTITY")
        val quantity: String?,
        /** Единица измерения */
        @SerializedName("MEINS")
        val meins: String?
)