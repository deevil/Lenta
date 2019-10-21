package com.lenta.bp14.requests.pojo

import com.google.gson.annotations.SerializedName

data class ExpectedDelivery(
        /** Тип поставки */
        @SerializedName("TYPE_DELIV") //
        val type: String,
        /** Статус поставки */
        @SerializedName("STAT_DELIV") //
        val status: String,
        /** Количество в заказе */
        @SerializedName("MENGE") //
        val quantityInOrder: Double,
        /** Количество в входящей поставке */
        @SerializedName("ORMNG") //
        val quantityInDelivery: Double,
        /** ЕИ заказа на поставку */
        @SerializedName("BSTME") //
        val unitsCode: String,
        /** Планируемая дата поставки */
        @SerializedName("DATE_PLAN") //
        val date: String,
        /** Планируемое время поставки */
        @SerializedName("TIME_PLAN") //
        val time: String
)