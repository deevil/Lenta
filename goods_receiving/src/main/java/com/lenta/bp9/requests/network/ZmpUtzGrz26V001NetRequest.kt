package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.revise.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ZmpUtzGrz26V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz26V001Result, ZmpUtzGrz26V001Params> {
    override suspend fun run(params: ZmpUtzGrz26V001Params): Either<Failure, ZmpUtzGrz26V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_26_V001", params, ZmpUtzGrz26V001Status::class.java)
    }
}

data class ZmpUtzGrz26V001Params(
        @SerializedName("IV_TASK_NUM") //номер задания
        val taskNumber: String,
        @SerializedName("IV_EXIDV_TOP") //номер ГЕ
        val cargoUnitNumber: String,
        @SerializedName("IV_MATNR") //номер товара
        val materialNumber: String
)

class ZmpUtzGrz26V001Status : ObjectRawStatus<ZmpUtzGrz26V001Result>()

data class ZmpUtzGrz26V001Result(
        @SerializedName("ET_EXIDV") //Таблица ЕО по товару
        val processingUnits: List<TaskProcessingUnitInfoRestData>,
        @SerializedName("ET_PARTS") //Таблица партий задания
        val taskBatches: List<TaskBatchInfoRestData>,
        @SerializedName("ET_PROD_TEXT")//Таблица ЕГАИС производителей
        val manufacturers: List<Manufacturer>,
        @SerializedName("ET_TASK_BOX") //Список коробок задания для передачи в МП
        val taskBoxes: List<TaskBoxInfoRestData>,
        @SerializedName("ET_TASK_MARK") //Список марок задания для передачи в МП
        val taskExciseStamps: List<TaskExciseStampRestData>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse