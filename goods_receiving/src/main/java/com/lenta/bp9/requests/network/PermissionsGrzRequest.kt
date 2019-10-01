package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class PermissionsGrzRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<PermissionsGrzResult, Nothing?> {
    override suspend fun run(params: Nothing?): Either<Failure, PermissionsGrzResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_01_V001", null, PermissionGrzStatus::class.java)
    }
}

class PermissionGrzStatus : ObjectRawStatus<PermissionsGrzResult>()


data class PermissionsGrzResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class Market(
        @SerializedName("WERKS")
        val number: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)