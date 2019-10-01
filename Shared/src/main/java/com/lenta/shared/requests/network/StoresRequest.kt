package com.lenta.shared.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject


class StoresRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<StoresRequestResult, Nothing?> {
    override suspend fun run(params: Nothing?): Either<Failure, StoresRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WOB_01_V001", params, StoresRequestStatus::class.java)
    }
}

class StoresRequestStatus : ObjectRawStatus<StoresRequestResult>()

data class StoresRequestResult(
        @SerializedName("ET_WERKS")
        val markets: List<Market>,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse

data class Market(
        @SerializedName("WERKS")
        val number: String,
        @SerializedName("ADDRES")
        val address: String,
        @SerializedName("RETAIL_TYPE")
        val retailType: String
)