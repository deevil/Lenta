package com.lenta.bp14.requests.work_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.models.check_list.CheckListTaskDescription
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class ExpectedDeliveriesNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : IExpectedDeliveriesNetRequest {
    override suspend fun run(params: ExpectedDeliveriesParams): Either<Failure, ExpectedDeliveriesResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_13_V001", params, ExpectedDeliveriesStatus::class.java)
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

interface IExpectedDeliveriesNetRequest : UseCase<ExpectedDeliveriesResult, ExpectedDeliveriesParams>

data class ExpectedDeliveriesParams(
        @SerializedName("IV_WERKS") // Номер ТК
        val tkNumber: String,
        @SerializedName("IV_MATNR") // Номер товара
        val material: String
)

class ExpectedDeliveriesStatus : ObjectRawStatus<ExpectedDeliveriesResult>()

data class ExpectedDeliveriesResult(
        @SerializedName("ET_PLAN_DELIV") // Таблица планируемых поставок
        val delivery: List<ExpectedDelivery>,
        @SerializedName("ET_RETCODE") // Код возврата + Текст ошибки
        val retCodes: List<RetCode>
)

data class ExpectedDelivery(
        @SerializedName("TYPE_DELIV") // Тип поставки
        val type: String,
        @SerializedName("STAT_DELIV") // Статус поставки
        val status: String,
        @SerializedName("MENGE") // Количество в заказе
        val quantityInOrder: Double,
        @SerializedName("ORMNG") // Количество в входящей поставке
        val quantityInDelivery: Double,
        @SerializedName("BSTME") // ЕИ заказа на поставку
        val unitsCode: String,
        @SerializedName("DATE_PLAN") // Планируемая дата поставки
        val date: String,
        @SerializedName("TIME_PLAN") // Планируемое время поставки
        val time: String
)