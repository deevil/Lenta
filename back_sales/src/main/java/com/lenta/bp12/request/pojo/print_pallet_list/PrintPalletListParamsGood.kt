package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName

data class PrintPalletListParamsGood(
        /** SAP-код товара */
        @SerializedName("MATNR")
        val materialNumber: String,
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val basketNumber: String,
        /** Посчитанное количество */
        @SerializedName("FACT_QNT")
        val quantity: String,
        /** Базисная единица измерения */
        @SerializedName("MEINS")
        val uom: String
)