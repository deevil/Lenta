package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.*
import com.lenta.bp9.model.task.TaskExciseStampDiscrepanciesRestData
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class StartRecountPGENetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<StartRecountPGERestInfo, StartRecountPGEParams> {
    override suspend fun run(params: StartRecountPGEParams): Either<Failure, StartRecountPGERestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_30_V001", params, StartRecountPGEStatus::class.java)
    }
}

data class StartRecountPGEParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String, //Табельный номер
        @SerializedName("IV_DATE_COUNT")
        val dateRecount: String, //Дата начала пересчета
        @SerializedName("IV_TIME_COUNT")
        val timeRecount: String, //Время начала пересчета
        @SerializedName("IV_TASK_TYPE")
        val taskType: String
)

class StartRecountPGEStatus : ObjectRawStatus<StartRecountPGERestInfo>()

data class StartRecountPGERestInfo(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_TASK_POS") //Таблица состава задания ПГЕ	ZTT_GRZ_TASK_ED_POS_EXCH
        val taskComposition: List<TaskComposition>,
        @SerializedName("ET_TASK_DIFF") //Таблица расхождений по товару	ZTT_GRZ_TASK_DIF_EXCH
        val taskProductDiscrepancies: List<TaskProductDiscrepanciesRestData>,
        @SerializedName("ET_TASK_PARTS") //Таблица партий задания
        val taskBatches: List<TaskBatchInfoRestData>,
        @SerializedName("ET_PARTS_DIFF") //Таблица расхождений по партиям
        val taskBatchesDiscrepancies: List<TaskBatchesDiscrepanciesRestData>,
        @SerializedName("ET_TASK_BOX") //Список коробок задания для передачи в МП
        val taskBoxes: List<TaskBoxInfoRestData>,
        @SerializedName("ET_BOX_DIFF") //Таблица обработанных коробов
        val taskBoxesDiscrepancies: List<TaskBoxDiscrepanciesRestData>,
        @SerializedName("ET_TASK_MARK") //Список марок задания для передачи в МП
        val taskExciseStamps: List<TaskExciseStampInfoRestData>,
        @SerializedName("ET_MARK_DIFF") //Таблица обработанных марок задания
        val taskExciseStampsDiscrepancies: List<TaskExciseStampDiscrepanciesRestData>,
        @SerializedName("ET_VET_DIFF") //Таблица расхождений по вет.товарам
        val taskMercuryInfoRestData: List<TaskMercuryInfoRestData>,
        @SerializedName("ET_PROD_TEXT")//Таблица ЕГАИС производителей
        val manufacturers: List<Manufacturer>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse
