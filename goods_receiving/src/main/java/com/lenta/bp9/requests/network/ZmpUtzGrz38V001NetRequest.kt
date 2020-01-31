package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz38V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz38V001Result, ZmpUtzGrz38V001Params> {
    override suspend fun run(params: ZmpUtzGrz38V001Params): Either<Failure, ZmpUtzGrz38V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_38_V001", params, ZmpUtzGrz38V001Status::class.java)
    }
}

data class ZmpUtzGrz38V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IT_COND_CHECK")
        val transportConditions: List<TransportConditionRestData>
)

class ZmpUtzGrz38V001Status : ObjectRawStatus<ZmpUtzGrz38V001Result>()

data class ZmpUtzGrz38V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse