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

class ZmpUtzGrz35V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz35V001Result, ZmpUtzGrz35V001Params> {
    override suspend fun run(params: ZmpUtzGrz35V001Params): Either<Failure, ZmpUtzGrz35V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_35_V001", params, ZmpUtzGrz35V001Status::class.java)
    }
}

data class ZmpUtzGrz35V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_MODE") //Режима работы ФМ: «01» - ввод транспортировки «02» - пропуск ввода транспортировки
        val mode: String,
        @SerializedName("IV_TRNUM") //Номер транспортировки
        val shipmentNumber: String
)

class ZmpUtzGrz35V001Status : ObjectRawStatus<ZmpUtzGrz35V001Result>()

data class ZmpUtzGrz35V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse