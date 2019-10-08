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
        @SerializedName("IV_WERKS") // Номер ТК
        val tkNumber: String,
        @SerializedName("IV_MATNR") // Номер товара
        val material: String
)

class GoodSalesStatus : ObjectRawStatus<GoodSalesResult>()

data class GoodSalesResult(
        @SerializedName("ET_SALE_MATNR") // Таблица продажи товара
        val sales: List<GoodSales>,
        @SerializedName("ET_RETCODE") // Код возврата + Текст ошибки
        val retCodes: List<RetCode>
)

data class GoodSales(
        @SerializedName("LAST_SALE_DATE") // Дата последней продажи
        val lastSaleDate: String,
        @SerializedName("LAST_SALE_TIME") // Время последней продажи
        val lastSaleTime: String,
        @SerializedName("LAST_SALE_QNT") // Количество последней продажи
        val daySales: Double,
        @SerializedName("SALE_WEEK") // Количество продаж за 7 дней
        val weekSales: Double
)