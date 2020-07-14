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
        val personnelNumber: String, //Табельный номер
        @SerializedName("IV_UNBIND_VSD")
        val unbindVSD: String //общий флаг
)

class TransmittedStatus : ObjectRawStatus<TransmittedRestInfo>()

data class TransmittedRestInfo(
        @SerializedName("ES_TASK")
        val taskDescription: TaskDescriptionRestInfo, //Структура карточки задания
        @SerializedName("ET_NOTIFY")
        val notifications: List<TaskNotificationRestInfo>, //Таблица уведомлений
        @SerializedName("ET_DOC_PRINT") //Список документов для печати
        val listDocumentsPrinting: List<TaskDocumentsPrintingRestInfo>,
        @SerializedName("ET_VET_NOT_ACTUAL") //Список не актуальных ВСД
        val taskMercuryNotActualRestData: List<TaskMercuryNotActualRestData>,
        @SerializedName("ET_TASK_PERNR")
        val sectionsInfo: List<TaskSectionRestData>,
        @SerializedName("ET_MATNR_ABTNR")
        val sectionProducts: List<TaskSectionProductsRestData>,
        @SerializedName("EV_BKS_LIFNR")
        val supplierShipmentPP: String,
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?,
        @SerializedName("EV_RETCODE")
        override val retCode: Int
) : SapResponse