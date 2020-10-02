package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.platform.extentions.toDoubleOrZero

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
        /** SAP-Производитель партии */
        @SerializedName("PRODUCER_NAME")
        val producerName: String?,
        /** Количество */
        @SerializedName("QUANTITY")
        val quantity: String?,
        /** Единица измерения */
        @SerializedName("MEINS")
        val meins: String?,
        /** Срок годности */
        @SerializedName("DATE_EXPIR")
        val dateExpir: String?,
        /** Дата производства */
        @SerializedName("DATE_PROD")
        val dateProd: String?
) {
        fun toZPart() = ZPart(
                batch = batch.orEmpty(),
                stock = stock.orEmpty(),
                producer = producerName.orEmpty(),
                quantity = quantity.toDoubleOrZero(),
                meins = meins.orEmpty(),
                dateExpir = dateExpir.orEmpty(),
                dateProd = dateProd.orEmpty()
        )
}