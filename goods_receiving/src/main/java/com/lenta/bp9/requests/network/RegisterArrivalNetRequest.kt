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

class RegisterArrivalNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<RegisterArrivalRequestResult, RegisterArrivalRequestParameters> {
    override suspend fun run(params: RegisterArrivalRequestParameters): Either<Failure, RegisterArrivalRequestResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_06_V001", params, RegisterArrivalRequestStatus::class.java)
    }
}

data class RegisterArrivalRequestParameters(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_DATE_ARRIV")
        val dateArrival: String,
        @SerializedName("IV_TIME_ARRIV")
        val timeArrival: String,
        @SerializedName("IV_PAPER_TTN")
        val hasPaperTTN: String,
        @SerializedName("IV_SELF_REG")
        val isSelfRegistration: String

)

class RegisterArrivalRequestStatus : ObjectRawStatus<RegisterArrivalRequestResult>()


data class RegisterArrivalRequestResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_VBELN_EXIST")
        val shipmentExists: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse