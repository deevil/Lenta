package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskCargoUnitInfoRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz39V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz39V001Result, ZmpUtzGrz39V001Params> {
    override suspend fun run(params: ZmpUtzGrz39V001Params): Either<Failure, ZmpUtzGrz39V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_39_V001", params, ZmpUtzGrz39V001Status::class.java)
    }
}

data class ZmpUtzGrz39V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_DATE_COUNT") //Дата начала пересчета
        val recountStartDate: String,
        @SerializedName("IV_TIME_COUNT") //Время начала пересчета
        val recountStartTime: String
)

class ZmpUtzGrz39V001Status : ObjectRawStatus<ZmpUtzGrz39V001Result>()

data class ZmpUtzGrz39V001Result(
        @SerializedName("ET_EXIDV_TOP")
        val cargoUnits: List<TaskCargoUnitInfoRestData>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse