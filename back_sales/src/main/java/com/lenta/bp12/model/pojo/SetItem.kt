package com.lenta.bp12.model.pojo

import com.google.gson.annotations.SerializedName

data class SetItem(
        /** Номер товара? */
        @SerializedName("MATNR_OSN")
        var ean: String,
        /** Sap-код товара */
        @SerializedName("MATNR")
        var material: String,
        /** Количество вложенного */
        @SerializedName("MENGE")
        var innerQuantity: String,
        /** Базовая единица измерения */
        @SerializedName("MEINS")
        var unitCode: String
)