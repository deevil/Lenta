package com.lenta.shared.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ServerTimeRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ServerTime, ServerTimeRequestParam> {

    override suspend fun run(params: ServerTimeRequestParam): Either<Failure, ServerTime> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_87_V001", params, ServerTimeRequestStatus::class.java)
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
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse