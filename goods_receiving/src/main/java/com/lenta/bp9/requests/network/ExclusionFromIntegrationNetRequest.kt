package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ExclusionFromIntegrationNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ExclusionFromIntegrationResult, ExclusionFromIntegrationParameters> {
    override suspend fun run(params: ExclusionFromIntegrationParameters): Either<Failure, ExclusionFromIntegrationResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_33_V001", params, ExclusionFromIntegrationStatus::class.java)
    }
}

data class ExclusionFromIntegrationParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_ REASON")
        val reason: String
)

class ExclusionFromIntegrationStatus : ObjectRawStatus<ExclusionFromIntegrationResult>()

data class ExclusionFromIntegrationResult(
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse