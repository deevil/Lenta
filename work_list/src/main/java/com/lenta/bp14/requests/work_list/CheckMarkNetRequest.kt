package com.lenta.bp14.requests.work_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.MarkStatus
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class CheckMarkNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : ICheckMarkNetRequest {
    override suspend fun run(params: CheckMarkParams): Either<Failure, CheckMarkResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_12_V001", params, CheckMarkStatus::class.java)
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

interface ICheckMarkNetRequest : UseCase<CheckMarkResult, CheckMarkParams>

data class CheckMarkParams(
        /** Номер ТК */
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        /** Номер товара */
        @SerializedName("IV_MATNR")
        val material: String,
        /** Номер марки */
        @SerializedName("IV_MARK_NUM")
        val markNumber: String,
        /** Режим обработки: 1 - Алкогольная марка, 2 - Контрольная марка */
        @SerializedName("IV_MODE")
        val mode: Int
)

class CheckMarkStatus : ObjectRawStatus<CheckMarkResult>()

data class CheckMarkResult(
        /** Таблица результатов проверки */
        @SerializedName("ET_RESULT")
        val markStatus: List<MarkStatus>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)