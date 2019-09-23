package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class RejectNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<RejectRequestResult, RejectRequestParameters>() {
    override suspend fun run(params: RejectRequestParameters): Either<Failure, RejectRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_19_V001", params, RejectRequestStatus::class.java)
    }
}

class RejectRequestStatus : ObjectRawStatus<RejectRequestResult>()


data class RejectRequestResult(
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class RejectRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_FAIL_MODE")
        val rejectMode: String,
        @SerializedName("IV_ST_TEXT")
        val rejectReason: String
)