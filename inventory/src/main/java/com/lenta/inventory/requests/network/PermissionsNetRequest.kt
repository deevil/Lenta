package com.lenta.inventory.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject

class PermissionsRequest
@Inject constructor(private val hyperHive: HyperHive, private val gson: Gson) : UseCase<PermissionsResult, PermissionsParams>() {
    override suspend fun run(params: PermissionsParams): Either<Failure, PermissionsResult> {
        //TODO (DB) нужно добавить поддержку логина пользователя когда доработают ФМ модуль
        val webCallParams = WebCallParams().apply {
            data = gson.toJson(params)
            headers = mapOf(
                    "X-SUP-DOMAIN" to "DM-MAIN",
                    "Content-Type" to "application/json"
            )
        }
        val stringStatus = hyperHive.requestAPI.web("ZMP_UTZ_99_V001", webCallParams).execute()
        val status = gson.fromJson(stringStatus, PermissionInventoryStatus::class.java)
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

data class PermissionsParams(val login: String)


class PermissionInventoryStatus : ObjectRawStatus<PermissionsResult>()


data class PermissionsResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("EV_AUTH_COUNT")
        val authCount: String,
        @SerializedName("EV_AUTH_PLACE")
        val authPlace: String,
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