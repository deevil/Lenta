package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class DeclareTransportDefectNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<DeclareTransportDefectRestInfo, DeclareTransportDefectParams> {
    override suspend fun run(params: DeclareTransportDefectParams): Either<Failure, DeclareTransportDefectRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_11_V001", params, DeclareTransportDefectStatus::class.java)
    }
}

data class DeclareTransportDefectParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String //Табельный номер
)

class DeclareTransportDefectStatus : ObjectRawStatus<DeclareTransportDefectRestInfo>()

data class DeclareTransportDefectRestInfo(
        @SerializedName("ET_EXIDV_TOP")
        val cargoUnits: List<TaskCargoUnitInfoRestData>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse