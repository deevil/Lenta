package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class ExciseInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ExciseInfoResult, ExciseInfoParams> {

    override suspend fun run(params: ExciseInfoParams): Either<Failure, ExciseInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_100_V001", params, ExciseInfoStatus::class.java)
    }

}

data class ExciseInfoParams(
        /** Код бизнес процесса */
        @SerializedName("IV_CODEBP")
        val bpCode: String,
        /** Фактическое количество */
        @SerializedName("IV_WIV_FACT_QNTERKS")
        val quantity: String,
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Номер товара? */
        @SerializedName("IV_MATNR_COMP")
        val materialComp: String,
        /** Код акцизной марки */
        @SerializedName("IV_MARK_NUM")
        val markNumber: String,
        /** Номер коробки */
        @SerializedName("IV_BOX_NUM")
        val boxNumber: String,
        /** ЕГАИС Код организации */
        @SerializedName("IV_ZPROD")
        val entityCode: String,
        /** УТЗ ТСД: Дата розлива */
        @SerializedName("IV_BOTT_MARK")
        val bottledDate: String,
        /** Режим работы: 1 - проверка марки, 2 - проверка коробки, 3 - проверка партионных признаков */
        @SerializedName("IV_MODE")
        val mode: Int
)

class ExciseInfoStatus : ObjectRawStatus<ExciseInfoResult>()

data class ExciseInfoResult(
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
        val marks: String,
        /** Номер товара? */
        @SerializedName("EV_MATNR_COMP")
        val materialComp: String,
        /** Таблица ЕГАИС производителей */
        @SerializedName("ET_PROD_TEXT")
        val producers: String,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse