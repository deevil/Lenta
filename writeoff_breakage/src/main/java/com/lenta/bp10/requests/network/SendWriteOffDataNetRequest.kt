package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp10.requests.network.pojo.CreatedTaskInfo
import com.lenta.bp10.requests.network.pojo.ExciseStamp
import com.lenta.bp10.requests.network.pojo.MaterialNumber
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SendWriteOffDataNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<SendWriteOffDataResult, SendWriteOffDataParams> {
    override suspend fun run(params: SendWriteOffDataParams): Either<Failure, SendWriteOffDataResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = "ZMP_UTZ_WOB_04_V001",
                data = params,
                clazz = WriteOffReportStatus::class.java)
    }
}

data class SendWriteOffDataParams(

        // <summary>
        // Табельный номер
        // </summary>
        @SerializedName("IV_PERNR")
        val perNo: String,

        // <summary>
        // Принтер
        // </summary>
        @SerializedName("IV_PRINTERNAME")
        val printer: String,

        // <summary>
        // Название задания
        // </summary>
        @SerializedName("IV_DESCR")
        val taskName: String,

        // <summary>
        // Тип задания на списание
        // </summary>
        @SerializedName("IV_TYPE")
        val taskType: String,

        // <summary>
        // Предп
        // </summary>
        @SerializedName("IV_WERKS")
        val tkNumber: String,

        // <summary>
        // Склад
        // </summary>
        @SerializedName("IV_LGORT")
        val storloc: String,

        // <summary>
        // IP адрес ТСД
        // </summary>
        @SerializedName("IV_IP")
        val ipAdress: String,

        // <summary>
        // Список товаров для сохранения задания из ТСД
        // </summary>
        @SerializedName("IT_MATERIALS")
        val materials: List<MaterialNumber>,

        // <summary>
        // Список марок для сохранения задания из ТСД
        // </summary>
        @SerializedName("IT_MARKS")
        val exciseStamps: List<ExciseStamp>


)

class WriteOffReportStatus : ObjectRawStatus<SendWriteOffDataResult>()

data class SendWriteOffDataResult(
        @SerializedName("ET_TASK_LIST")
        val taskList: List<CreatedTaskInfo>,
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        @SerializedName("EV_RETCODE")
        val retCode: String
)
