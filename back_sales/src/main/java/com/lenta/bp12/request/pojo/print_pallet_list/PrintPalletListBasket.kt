package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName

data class PrintPalletListBasket(
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        var number: String,
        /** Наименование */
        @SerializedName("BASKET_NAME")
        var description: String,
        /** Номер секции */
        @SerializedName("ABTNR")
        var section: String
)