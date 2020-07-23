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

class ZmpUtzGrz37V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz37V001Result, ZmpUtzGrz37V001Params> {
    override suspend fun run(params: ZmpUtzGrz37V001Params): Either<Failure, ZmpUtzGrz37V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_37_V001", params, ZmpUtzGrz37V001Status::class.java)
    }
}

data class ZmpUtzGrz37V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_DATE_LOAD") //Дата начала погрузки
        val loadingStartDate: String,
        @SerializedName("IV_TIME_LOAD") //Время начала погрузки
        val loadingStartTime: String
)

class ZmpUtzGrz37V001Status : ObjectRawStatus<ZmpUtzGrz37V001Result>()

data class ZmpUtzGrz37V001Result(
        @SerializedName("ET_COND_CHECK")
        val transportConditions: List<TransportConditionRestData>,
        @SerializedName("ET_COND_NOTIFY")
        val conditionNotifications: List<TaskNotificationRestInfo>,
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse