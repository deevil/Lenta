package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class TaskCardNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<TaskCardRequestResult, TaskCardParams>() {
    override suspend fun run(params: TaskCardParams): Either<Failure, TaskCardRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_05_V001", params, TaskCardRequestStatus::class.java)
    }
}

class TaskCardRequestStatus : ObjectRawStatus<TaskCardRequestResult>()


data class TaskCardRequestResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

data class TaskCardParams(
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)