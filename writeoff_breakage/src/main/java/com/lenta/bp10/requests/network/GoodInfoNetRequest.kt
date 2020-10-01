package com.lenta.bp10.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.bp10.requests.network.pojo.Property
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import com.lenta.shared.requests.combined.scan_info.pojo.Material
import javax.inject.Inject

class GoodInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<GoodInfoResult, GoodInfoParams> {

    override suspend fun run(params: GoodInfoParams): Either<Failure, GoodInfoResult> {
        return fmpRequestsHelper.restRequest(RESOURCE_NAME, params, GoodInfoStatus::class.java)
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_WOB_02_V001"
    }

}

data class GoodInfoParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String = "",
        /** ШК товара */
        @SerializedName("IV_EAN")
        val ean: String = ""
)

class GoodInfoStatus : ObjectRawStatus<GoodInfoResult>()

data class GoodInfoResult(
        /** Инфо по ШК номеру */
        @SerializedName("ES_EAN")
        val ean: Ean?,
        /** Инфо по сап номеру */
        @SerializedName("ES_MATERIAL")
        val material: Material?,
        /** Инфо по набору */
        @SerializedName("ET_SET")
        val set: List<Set>?,
        /** Свойства по товарам */
        @SerializedName("ET_PROPERTIES")
        val properties: List<Property>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse