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
        val sectionsInfo: List<TaskSectionRestData> //таблица секция\представитель
)

class SubmittedStatus : ObjectRawStatus<SubmittedRestInfo>()

data class SubmittedRestInfo(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo, //Структура карточки задания
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>, //Таблица уведомлений
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse