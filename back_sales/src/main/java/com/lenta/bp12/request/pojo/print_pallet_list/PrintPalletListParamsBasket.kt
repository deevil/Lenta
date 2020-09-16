package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName

data class PrintPalletListParamsBasket(
        /** Номер корзины */
        @SerializedName("BASKET_NUM")
        val number: String?,
        /** Наименование */
        @SerializedName("BASKET_NAME")
        val description: String?,
        /** Номер секции */
        @SerializedName("ABTNR")
        val section: String?
)