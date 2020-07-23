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

class FixationDepartureReceptionDistrCenterNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<FixationDepartureReceptionDistrCenterResult, FixationDepartureReceptionDistrCenterParameters> {
    override suspend fun run(params: FixationDepartureReceptionDistrCenterParameters): Either<Failure, FixationDepartureReceptionDistrCenterResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_23_V001", params, FixationDepartureReceptionDistrCenterStatus::class.java)
    }
}

data class FixationDepartureReceptionDistrCenterParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IS_SEALS")
        val fillings: List<String>
)

class FixationDepartureReceptionDistrCenterStatus : ObjectRawStatus<FixationDepartureReceptionDistrCenterResult>()

data class FixationDepartureReceptionDistrCenterResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse