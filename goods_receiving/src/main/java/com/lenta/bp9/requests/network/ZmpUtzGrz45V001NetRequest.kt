package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskCargoUnitInfoRestData
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.TaskZBatchesDiscrepanciesRestData
import com.lenta.bp9.model.task.revise.TransportConditionRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz45V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz45V001Result, ZmpUtzGrz45V001Params> {
    override suspend fun run(params: ZmpUtzGrz45V001Params): Either<Failure, ZmpUtzGrz45V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_45_V001", params, ZmpUtzGrz45V001Status::class.java)
    }
}

data class ZmpUtzGrz45V001Params(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IT_ZPARTS_DIFF")
        val discrepanciesZBatches: List<TaskZBatchesDiscrepanciesRestData>
)

class ZmpUtzGrz45V001Status : ObjectRawStatus<ZmpUtzGrz45V001Result>()

data class ZmpUtzGrz45V001Result(
        @SerializedName("ET_ZPARTS_DIFF")
        val taskZBatchesDiscrepancies: List<TaskZBatchesDiscrepanciesRestData>?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse