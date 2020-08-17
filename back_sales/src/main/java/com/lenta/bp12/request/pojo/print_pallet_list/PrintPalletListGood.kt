package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName

data class PrintPalletListGood(
        /** SAP-код товара */
        @SerializedName("MATNR")
        var materialNumber: String,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var basketNumber: String,
        /** Посчитанное количество */
        @SerializedName("FACT_QNT")
        var quantity: String,
        /** Базисная единица измерения */
        @SerializedName("MEINS")
        var uom: String
)