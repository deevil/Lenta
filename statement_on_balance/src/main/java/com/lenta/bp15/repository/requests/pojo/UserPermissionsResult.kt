package com.lenta.bp15.repository.requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class UserPermissionsResult(
        /** Список адресов ТК */
        @SerializedName("ET_WERKS")
        val markets: List<MarketRawInfo>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>
) : IResultWithRetCodes