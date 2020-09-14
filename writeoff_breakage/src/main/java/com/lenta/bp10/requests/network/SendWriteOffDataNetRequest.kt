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
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val perNo: String,
        /** Название задания */
        @SerializedName("IV_DESCR")
        val taskName: String,
        /** Тип задания на списание */
        @SerializedName("IV_TYPE")
        val taskType: String,
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Склад */
        @SerializedName("IV_LGORT")
        val storloc: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val ipAdress: String,
        /** Список товаров для сохранения задания из ТСД */
        @SerializedName("IT_MATERIALS")
        val materials: List<MaterialNumber>,
        /** Список марок для сохранения задания из ТСД */
        @SerializedName("IT_MARKS")
        val exciseStamps: List<ExciseStamp>,
        /** Принтер */
        @SerializedName("IV_PRINTERNAME")
        val printer: String
)

class WriteOffReportStatus : ObjectRawStatus<SendWriteOffDataResult>()

data class SendWriteOffDataResult(
        /** Список созданных заданий */
        @SerializedName("ET_TASK_LIST")
        val taskList: List<CreatedTaskInfo>,
        /** Код возврата */
        @SerializedName("EV_ERROR_TEXT")
        val errorText: String,
        /** Текст ошибки */
        @SerializedName("EV_RETCODE")
        val retCode: String
)