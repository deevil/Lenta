package com.lenta.bp18.request.model.result

import com.google.gson.annotations.SerializedName
import com.lenta.shared.requests.SapResponse

data class MarketInfoResult (
        @SerializedName("EV_WERKS")
        val marketNumber: String,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
): SapResponse