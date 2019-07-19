package com.lenta.shared.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject


class ServerTimeRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<ServerTime, ServerTimeRequestParam>() {
    override suspend fun run(params: ServerTimeRequestParam): Either<Failure, ServerTime> {
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        webCallParams.data = "{\"IV_WERKS\":\"${params.tkNumber}\"}"
        val status = hyperHive.requestAPI.web("ZMP_UTZ_87_V001", webCallParams, ServerTimeRequestStatus::class.java).execute()
        Logg.d("ServerTimeRequestStatus $status")

        if (status.isNotBad()) {
            val errorText = status.result?.raw?.errorText
            return if (errorText.isNullOrEmpty()) {
                Either.Right(status.result!!.raw!!)
            } else {
                ANALYTICS_HELPER?.onRetCodeNotEmpty(status = status.toString())
                Either.Left(Failure.SapError(errorText))
            }
        }

        return Either.Left(status.getFailure())
    }
}


data class ServerTimeRequestParam(
        @SerializedName("IV_WERKS")
        val tkNumber: String)

class ServerTimeRequestStatus : ObjectRawStatus<ServerTime>()


data class ServerTime(
        @SerializedName("EV_DATE")
        val date: String,
        @SerializedName("EV_TIME")
        val time: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: String
)