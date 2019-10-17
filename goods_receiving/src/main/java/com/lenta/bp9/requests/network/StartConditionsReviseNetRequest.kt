package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class StartConditionsReviseNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<StartConditionsReviseRequestResult, StartConditionsReviseRequestParameters> {
    override suspend fun run(params: StartConditionsReviseRequestParameters): Either<Failure, StartConditionsReviseRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_09_V001", params, StartConditionsReviseRequestStatus::class.java)
    }
}

class StartConditionsReviseRequestStatus : ObjectRawStatus<StartConditionsReviseRequestResult>()


data class StartConditionsReviseRequestResult(
        @SerializedName("ET_COND_CHECK")
        val conditions: List<TransportConditionRestData>,
        @SerializedName("ET_COND_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,

        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class StartConditionsReviseRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_DATE_UNLOAD")
        val unloadStartDate: String,
        @SerializedName("IV_TIME_UNLOAD")
        val unloadStartTime: String
)