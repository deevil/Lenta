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

class UnloadingStartReceptionDistrCenterNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<UnloadingStartReceptionDistrCenterResult, UnloadingStartReceptionDistrCenterParameters> {
    override suspend fun run(params: UnloadingStartReceptionDistrCenterParameters): Either<Failure, UnloadingStartReceptionDistrCenterResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_20_V001", params, UnloadingStartReceptionDistrCenterStatus::class.java)
    }
}

data class UnloadingStartReceptionDistrCenterParameters(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IV_DATE_UNLOAD")
        val unloadStartDate: String,
        @SerializedName("IV_TIME_UNLOAD")
        val unloadStartTime: String,
        @SerializedName("IV_TASK_TYPE")
        val taskType: String
)

class UnloadingStartReceptionDistrCenterStatus : ObjectRawStatus<UnloadingStartReceptionDistrCenterResult>()

data class UnloadingStartReceptionDistrCenterResult(
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