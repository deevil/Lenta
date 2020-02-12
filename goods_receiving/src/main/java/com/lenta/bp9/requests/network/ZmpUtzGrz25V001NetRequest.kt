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

class ZmpUtzGrz25V001NetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ZmpUtzGrz25V001Result, ZmpUtzGrz25V001Params> {
    override suspend fun run(params: ZmpUtzGrz25V001Params): Either<Failure, ZmpUtzGrz25V001Result> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_25_V001", params, ZmpUtzGrz25V001Status::class.java)
    }
}

data class ZmpUtzGrz25V001Params(
        @SerializedName("IV_IP")
        val deviceIP: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        @SerializedName("IV_PERNR")
        val personalNumber: String,
        @SerializedName("IT_DEFECTS") //Таблица транспортного брака
        val transportMarriage: List<TaskTransportMarriageInfoRestData>,
        @SerializedName("IT_BOX_DIFF") //Таблица обработанных коробов
        val processedBoxInfo: List<TaskProcessedBoxInfoRestData>,
        @SerializedName("IT_MARK_DIFF") //Таблица обработанных марок задания
        val processedExciseStamp: List<TaskProcessedExciseStampRestData>,
        @SerializedName("IV_SAVE") // Сохранить результаты ( если пусто - только смена статуса)
        val isSave: String,
        @SerializedName("IV_PRINTERNAME") //Спул: длинные имена устройств
        val printerName: String
)

class ZmpUtzGrz25V001Status : ObjectRawStatus<ZmpUtzGrz25V001Result>()

data class ZmpUtzGrz25V001Result(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo,
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>,
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse