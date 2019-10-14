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
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String
)

class ExpectedDeliveriesStatus : ObjectRawStatus<ExpectedDeliveriesResult>()

data class ExpectedDeliveriesResult(
        /** Таблица планируемых поставок */
        @SerializedName("ET_PLAN_DELIV")
        val deliveries: List<ExpectedDelivery>,
        /** Код возврата + Текст ошибки */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)

data class ExpectedDelivery(
        /** Тип поставки */
        @SerializedName("TYPE_DELIV") //
        val type: String,
        /** Статус поставки */
        @SerializedName("STAT_DELIV") //
        val status: String,
        /** Количество в заказе */
        @SerializedName("MENGE") //
        val quantityInOrder: Double,
        /** Количество в входящей поставке */
        @SerializedName("ORMNG") //
        val quantityInDelivery: Double,
        /** ЕИ заказа на поставку */
        @SerializedName("BSTME") //
        val unitsCode: String,
        /** Планируемая дата поставки */
        @SerializedName("DATE_PLAN") //
        val date: String,
        /** Планируемое время поставки */
        @SerializedName("TIME_PLAN") //
        val time: String
)