package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class FinishConditionsReviseNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<FinishConditionsReviseRequestResult, FinishConditionsReviseRequestParameters> {
    override suspend fun run(params: FinishConditionsReviseRequestParameters): Either<Failure, FinishConditionsReviseRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_10_V001", params, FinishConditionsReviseRequestStatus::class.java)
    }
}

class FinishConditionsReviseRequestStatus : ObjectRawStatus<FinishConditionsReviseRequestResult>()


data class FinishConditionsReviseRequestResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class FinishConditionsReviseRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("ET_COND_CHECK")
        val conditions: List<TransportConditionRestData>
)