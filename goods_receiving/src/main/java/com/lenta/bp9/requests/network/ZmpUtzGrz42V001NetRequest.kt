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

class ZmpUtzGrz42V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz42V001Result, ZmpUtzGrz42V001Params> {
    override suspend fun run(params: ZmpUtzGrz42V001Params): Either<Failure, ZmpUtzGrz42V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_42_V001", params, ZmpUtzGrz42V001Status::class.java)
    }
}

data class ZmpUtzGrz42V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String
)

class ZmpUtzGrz42V001Status : ObjectRawStatus<ZmpUtzGrz42V001Result>()

data class ZmpUtzGrz42V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse