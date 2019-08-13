package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class MarketOverIPRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<MarketOverIPRestInfo, MarketOverIPParams>() {
    override suspend fun run(params: MarketOverIPParams): Either<Failure, MarketOverIPRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_89_V001", params, MarketOverIPStatus::class.java)
    }
}

data class MarketOverIPParams(
        @SerializedName("IV_IP")
        val ipAdress: String,
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_WERKS")
        val werks: String
)

class MarketOverIPStatus : ObjectRawStatus<MarketOverIPRestInfo>()


data class MarketOverIPRestInfo(
        @SerializedName("EV_WERKS")
        val marketNumber: String,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int

) : SapResponse
