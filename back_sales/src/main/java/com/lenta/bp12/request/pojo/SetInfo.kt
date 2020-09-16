package com.lenta.bp12.request.pojo

import com.google.gson.annotations.SerializedName

/**
 * usages:
 * @see com.lenta.bp12.request.pojo.good_info.GoodInfoResult
 * */
data class SetInfo(
        /** Номер товара? */
        @SerializedName("MATNR_OSN")
        val ean: String?,
        /** Sap-код товара */
        @SerializedName("MATNR")
        val material: String?,
        /** Количество вложенного */
        @SerializedName("MENGE")
        val innerQuantity: String?,
        /** Базовая единица измерения */
        @SerializedName("MEINS")
        val unitCode: String?
)