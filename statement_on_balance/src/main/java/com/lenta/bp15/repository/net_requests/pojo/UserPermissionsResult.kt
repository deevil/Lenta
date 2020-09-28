package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.requests.SapResponse

data class UserPermissionsResult(
        /** Список адресов ТК */
        @SerializedName("ET_WERKS")
        val marketInfos: List<MarketInfo>,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse