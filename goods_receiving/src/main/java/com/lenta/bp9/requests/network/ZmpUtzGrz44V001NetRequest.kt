package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskExciseStampInfoRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz44V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz44V001Result, ZmpUtzGrz44V001Params> {
    override suspend fun run(params: ZmpUtzGrz44V001Params): Either<Failure, ZmpUtzGrz44V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_44_V001", params, ZmpUtzGrz44V001Status::class.java)
    }
}

data class ZmpUtzGrz44V001Params(
        @SerializedName("IV_MATNR")     //номер товара
        val materialNumber: String,
        @SerializedName("IV_TASK_NUM")  //текущий номер задани
        val taskNumber: String,
        @SerializedName("IV_MARK_NUM")   //отсканированная марка
        val stampCode: String,
        @SerializedName("IV_BLOCK_NUM")  //отсканированный блок
        val blockCode: String,
        @SerializedName("IV_BOX_NUM")     //отсканированный короб
        val boxCode: String
)

class ZmpUtzGrz44V001Status : ObjectRawStatus<ZmpUtzGrz44V001Result>()

data class ZmpUtzGrz44V001Result(
        @SerializedName("EV_STAT")  //индикатор из одной позиции
        val indicatorOnePosition: String,
        @SerializedName("ET_MARKS") //Список марок задания для передачи в МП
        val taskExciseStamps: List<TaskExciseStampInfoRestData>,
        @SerializedName("EV_EXIDV_TOP") //Номер ГЕ
        val cargoUnitNumber: String,
        @SerializedName("ES_MATNR") //Данные по товару излишку для ПГЕ
        val productSurplusDataPGE: ProductSurplusDataPGERestData,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String,
        @SerializedName("EV_STAT_TEXT")
        val markStatusText: String
) : SapResponse