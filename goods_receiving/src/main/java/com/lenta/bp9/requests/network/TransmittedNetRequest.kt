package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class TransmittedNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<TransmittedRestInfo, TransmittedParams> {
    override suspend fun run(params: TransmittedParams): Either<Failure, TransmittedRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_13_V001", params, TransmittedStatus::class.java)
    }
}

data class TransmittedParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String //Табельный номер
)

class TransmittedStatus : ObjectRawStatus<TransmittedRestInfo>()

data class TransmittedRestInfo(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo, //Структура карточки задания
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>, //Таблица уведомлений
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)