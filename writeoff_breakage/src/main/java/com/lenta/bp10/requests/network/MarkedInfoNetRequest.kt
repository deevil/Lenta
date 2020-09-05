package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp10.requests.network.pojo.MarkInfo
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class MarkedInfoNetRequest
@Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<MarkedInfoResult, MarkedInfoParams> {

    override suspend fun run(params: MarkedInfoParams): Either<Failure, MarkedInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WOB_07_V001", params, MarkedInfoStatus::class.java)
    }

}

data class MarkedInfoParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Номер короба */
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String = "",
        /** Номер марки */
        @SerializedName("IV_MARK_NUM")
        val markNumber: String = "",
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Код бизнес процесса */
        @SerializedName("IV_CODEBP")
        val bpCode: String = "WOB"
)

class MarkedInfoStatus : ObjectRawStatus<MarkedInfoResult>()

data class MarkedInfoResult(
        /** Статус */
        @SerializedName("EV_STAT")
        val status: String?,
        /** Текст статуса для отображения в МП */
        @SerializedName("EV_STAT_TEXT")
        val statusDescription: String?,
        /** Таблица марок в коробке */
        @SerializedName("ET_MARKS")
        val marks: List<MarkInfo>?,
        /** Свойства по товарам */
        @SerializedName("ET_PROPERTIES")
        val properties: List<Property>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse

