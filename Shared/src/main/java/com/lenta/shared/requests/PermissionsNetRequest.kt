package com.lenta.shared.requests

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class PermissionsRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PermissionsResult, PermissionsParams> {
    override suspend fun run(params: PermissionsParams): Either<Failure, PermissionsResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_99_V001", params, PermissionInventoryStatus::class.java)
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
        override val errorText: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse

data class Market(
        @SerializedName("WERKS")
        val number: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String,
        /**
         * Версия приложения для обновления через FMP
         */
        @SerializedName("VERSION")
        val version: String
)