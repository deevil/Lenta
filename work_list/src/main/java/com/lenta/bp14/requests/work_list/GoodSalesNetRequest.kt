package com.lenta.bp14.requests.work_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class GoodSalesNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : IGoodSalesNetRequest {
    override suspend fun run(params: GoodSalesParams): Either<Failure, GoodSalesResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_14_V001", params, GoodSalesStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }
}

interface IGoodSalesNetRequest : UseCase<GoodSalesResult, GoodSalesParams>

data class GoodSalesParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String
)

class GoodSalesStatus : ObjectRawStatus<GoodSalesResult>()

data class GoodSalesResult(
        /** Таблица продажи товара */
        @SerializedName("ET_SALE_MATNR")
        val sales: List<GoodSales>,
        /** Код возврата + Текст ошибки */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)

data class GoodSales(
        /** Дата последней продажи */
        @SerializedName("LAST_SALE_DATE")
        val lastSaleDate: String,
        /** Время последней продажи */
        @SerializedName("LAST_SALE_TIME")
        val lastSaleTime: String,
        /** Количество последней продажи */
        @SerializedName("LAST_SALE_QNT")
        val daySales: Double,
        /** Количество продаж за 7 дней */
        @SerializedName("SALE_WEEK")
        val weekSales: Double
)