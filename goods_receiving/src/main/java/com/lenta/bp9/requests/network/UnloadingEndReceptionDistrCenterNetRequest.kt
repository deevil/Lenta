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

class UnloadingEndReceptionDistrCenterNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<UnloadingEndReceptionDistrCenterResult, UnloadingEndReceptionDistrCenterParameters> {
    override suspend fun run(params: UnloadingEndReceptionDistrCenterParameters): Either<Failure, UnloadingEndReceptionDistrCenterResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_21_V001", params, UnloadingEndReceptionDistrCenterStatus::class.java)
    }
}

data class UnloadingEndReceptionDistrCenterParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IT_COND_CHECK")
        val transportConditions: List<TransportConditionRestData>,
        @SerializedName("IT_EXIDV_TOP")
        val cargoUnits: List<TaskCargoUnitInfoRestData>
)

class UnloadingEndReceptionDistrCenterStatus : ObjectRawStatus<UnloadingEndReceptionDistrCenterResult>()

data class UnloadingEndReceptionDistrCenterResult(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_NUM_SEALS")
        val quantityOutgoingFillings: String,
        @SerializedName("EV_DEFECT_ENABLE")
        val defectEnabled: String,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse