package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class UnlockTaskNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<UnlockTaskRequestResult, UnlockTaskRequestParameters> {
    override suspend fun run(params: UnlockTaskRequestParameters): Either<Failure, UnlockTaskRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_16_V001", params, UnlockTaskRequestStatus::class.java)
    }
}

class UnlockTaskRequestStatus : ObjectRawStatus<UnlockTaskRequestResult>()


data class UnlockTaskRequestResult(
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class UnlockTaskRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)