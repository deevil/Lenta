package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class EndRecountDirectDeliveriesNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<EndRecountDDResult, EndRecountDDParameters> {
    override suspend fun run(params: EndRecountDDParameters): Either<Failure, EndRecountDDResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_12_V001", params, EndRecountDDStatus::class.java)
    }
}

data class EndRecountDDParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IT_TASK_DIFF")
        val discrepanciesProduct: List<TaskProductDiscrepanciesRestData>,
        @SerializedName("IT_PARTS_DIFF")
        val discrepanciesBatches: List<TaskBatchesDiscrepanciesRestData>,
        @SerializedName("IT_VET_DIFF")
        val discrepanciesMercury: List<TaskMercuryDiscrepanciesRestData?>
)

class EndRecountDDStatus : ObjectRawStatus<EndRecountDDResult>()


data class EndRecountDDResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse