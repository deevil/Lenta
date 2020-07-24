package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskCargoUnitInfoRestData
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

class ZmpUtzGrz43V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz43V001Result, ZmpUtzGrz43V001Params> {
    override suspend fun run(params: ZmpUtzGrz43V001Params): Either<Failure, ZmpUtzGrz43V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_43_V001", params, ZmpUtzGrz43V001Status::class.java)
    }
}

data class ZmpUtzGrz43V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_MODE")
        val mode: String
)

class ZmpUtzGrz43V001Status : ObjectRawStatus<ZmpUtzGrz43V001Result>()

data class ZmpUtzGrz43V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("ET_COND_CHECK")
        val transportConditions: List<TransportConditionRestData>,
        @SerializedName("ET_EXIDV_TOP")
        val cargoUnits: List<TaskCargoUnitInfoRestData>,
        @SerializedName("ET_COND_NOTIFY")
        val conditionNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse