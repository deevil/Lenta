package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class GoodInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<GoodInfoResult, GoodInfoParams> {

    override suspend fun run(params: GoodInfoParams): Either<Failure, GoodInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_05_V001", params, GoodInfoStatus::class.java)
    }
}

data class GoodInfoParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** ШК товара */
        @SerializedName("IV_EAN")
        val ean: String,
        /** SAP-код товара */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Код бизнес процесса */
        @SerializedName("IV_CODEBP")
        val bpCode: String
)

class GoodInfoStatus : ObjectRawStatus<GoodInfoResult>()

// todo Структура данных пока не ясна, поправить при первом получении
data class GoodInfoResult(
        /** Справочник ШК */
        @SerializedName("ES_EAN")
        val eans: String,
        /** Справочник товаров */
        @SerializedName("ES_MATERIAL")
        val materials: String,
        /** Таблица наборов */
        @SerializedName("ET_SET")
        val sets: String,
        /** Таблица поставщиков */
        @SerializedName("ET_LIFNR")
        val suppliers: String,
        /** Таблица производителей */
        @SerializedName("ET_PROD")
        val producers: String,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String
) : SapResponse