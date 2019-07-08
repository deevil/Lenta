package com.lenta.bp7.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject


class StoresRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson, private val sessionInfo: ISessionInfo) : UseCase<StoresRequestResult, RequestParams>() {
    override suspend fun run(params: RequestParams): Either<Failure, StoresRequestResult> {
        //TODO (DB) нужно добавить поддержку логина пользователя когда доработают ФМ модуль
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json",
                    "Web-Authorization" to sessionInfo.basicAuth
            )
        }

        val status = hyperHive.requestAPI.web("ZMP_UTZ_WOB_01_V001", webCallParams, StoresRequestStatus::class.java).execute()
        Logg.d { "StoresRequest response: $status" }
        if (status.isNotBad()) {
            val errorText = status.result?.raw?.errorText
            return if (errorText.isNullOrEmpty()) {
                Either.Right(status.result!!.raw!!)
            } else {
                Either.Left(Failure.SapError(errorText))
            }
        }

        return Either.Left(status.getFailure())
    }
}

class StoresRequestStatus : ObjectRawStatus<StoresRequestResult>()

data class StoresRequestResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: String
)

data class Market(
        @SerializedName("WERKS")
        val number: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)