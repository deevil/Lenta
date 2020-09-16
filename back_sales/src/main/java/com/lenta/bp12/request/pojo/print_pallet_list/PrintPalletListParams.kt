package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName

data class PrintPalletListParams(
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP_PDA")
        val deviceIp: String,
        /** Код клиента
         * Заполнять в случае получения параметра, при получения списка заданий
         * при создании задания не заполнять */
        @SerializedName("KUNNR")
        val debitor: String = "",
        /** Таблица корзин задания */
        @SerializedName("IT_BASKET_INFO")
        val baskets: List<PrintPalletListParamsBasket>,
        /** Номер секции */
        @SerializedName("IT_MTNR_LIST")
        val goods: List<PrintPalletListParamsGood>
)