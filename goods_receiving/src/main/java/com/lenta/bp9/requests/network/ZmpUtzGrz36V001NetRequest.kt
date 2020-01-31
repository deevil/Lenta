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

class ZmpUtzGrz36V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz36V001Result, ZmpUtzGrz36V001Params> {
    override suspend fun run(params: ZmpUtzGrz36V001Params): Either<Failure, ZmpUtzGrz36V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_36_V001", params, ZmpUtzGrz36V001Status::class.java)
    }
}

data class ZmpUtzGrz36V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IS_DRIVER_DATA") //Структура данных водителя
        val driverData: TaskDriverDataInfoRestData,
        @SerializedName("IV_DATE_ARRIV") //Дата прибытия
        val arrivalDate: String,
        @SerializedName("IV_TIME_ARRIV") //Время прибытия
        val arrivalTime: String
)

class ZmpUtzGrz36V001Status : ObjectRawStatus<ZmpUtzGrz36V001Result>()

data class ZmpUtzGrz36V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse