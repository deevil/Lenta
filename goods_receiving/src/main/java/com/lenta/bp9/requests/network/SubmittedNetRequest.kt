package com.lenta.bp9.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp9.model.task.TaskDescriptionRestInfo
import com.lenta.bp9.model.task.TaskDocumentsPrinting
import com.lenta.bp9.model.task.TaskNotificationRestInfo
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SubmittedNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SubmittedRestInfo, SubmittedParams> {
    override suspend fun run(params: SubmittedParams): Either<Failure, SubmittedRestInfo> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_GRZ_14_V001", params, SubmittedStatus::class.java)
    }
}

data class SubmittedParams(
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String, //Номер задания
        @SerializedName("IV_IP")
        val deviceIP: String, //Ip адрес ТСД
        @SerializedName("IV_PERNR")
        val personnelNumber: String, //Табельный номер
        @SerializedName("IT_TASK_PERNR")
        val sectionsInfo: List<TaskSectionInfo> //таблица секция\представитель
)

class SubmittedStatus : ObjectRawStatus<SubmittedRestInfo>()

data class SubmittedRestInfo(
        @SerializedName("ET_DOC_PRINT")
        val listDocumentsPrinting: List<TaskDocumentsPrinting>, //Список документов для печати
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo, //Структура карточки задания
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>, //Таблица уведомлений
        @SerializedName("EV_RETCODE")
        val retcode: String,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)