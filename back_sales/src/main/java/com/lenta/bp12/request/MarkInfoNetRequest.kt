package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.MarkInfo
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class MarkInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<MarkInfoResult, MarkInfoParams> {

    override suspend fun run(params: MarkInfoParams): Either<Failure, MarkInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_100_V001", params, MarkInfoStatus::class.java)
    }

}

data class MarkInfoParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Номер товара */
        @SerializedName("IV_MATNR_COMP")
        val materialComp: String = "",
        /** Код акцизной марки */
        @SerializedName("IV_MARK_NUM")
        val markNumber: String,
        /** Номер коробки */
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String = "",
        /** ЕГАИС Код организации */
        @SerializedName("IV_ZPROD")
        val producerCode: String = "",
        /** УТЗ ТСД: Дата розлива */
        @SerializedName("IV_BOTT_MARK")
        val bottledDate: String = "",
        /** Режим работы: 1 - проверка марки, 2 - проверка коробки, 3 - проверка партионных признаков */
        @SerializedName("IV_MODE")
        val mode: Int,
        /** Фактическое количество */
        @SerializedName("IV_FACT_QNT")
        val quantity: Double,
        /** Фактическое количество */
        @SerializedName("IV_CODEBP")
        val codeBp: String = "BKS"
)

class MarkInfoStatus : ObjectRawStatus<MarkInfoResult>()

data class MarkInfoResult(
        /** Дата производства */
        @SerializedName("EV_DATEOFPOUR")
        val producedDate: String,
        /** Статус */
        @SerializedName("EV_STAT")
        val status: String,
        /** Текст статуса для отображения в МП */
        @SerializedName("EV_STAT_TEXT")
        val statusDescription: String,
        /** Таблица марок в коробке */
        @SerializedName("ET_MARKS")
        val marks: List<MarkInfo>,
        /** Номер товара? */
        @SerializedName("EV_MATNR_COMP")
        val materialComp: String,
        /** Таблица ЕГАИС производителей */
        @SerializedName("ET_PROD_TEXT")
        val producers: List<ProducerInfo>,
        /** Номер партии */
        @SerializedName("EV_ZCHARG")
        val partNumber: String,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse