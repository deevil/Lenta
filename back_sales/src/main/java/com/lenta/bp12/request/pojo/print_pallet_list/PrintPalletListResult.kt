package com.lenta.bp12.request.pojo.print_pallet_list

import com.google.gson.annotations.SerializedName
import com.lenta.shared.requests.SapResponse

data class PrintPalletListResult(
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse