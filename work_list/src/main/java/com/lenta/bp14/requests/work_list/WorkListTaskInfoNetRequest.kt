package com.lenta.bp14.requests.work_list

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class WorkListTaskInfoNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : IWorkListTaskInfoNetRequest {
    override suspend fun run(params: TaskInfoParams): Either<Failure, WorkListTaskInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_03_V001", params, WorkListTaskInfoStatus::class.java)
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

interface IWorkListTaskInfoNetRequest : UseCase<WorkListTaskInfoResult, TaskInfoParams>

class WorkListTaskInfoStatus : ObjectRawStatus<WorkListTaskInfoResult>()

data class WorkListTaskInfoResult(
        /** Таблица состава задания */
        @SerializedName("ET_TASK_POS")
        val positions: List<Position>,
        /** Таблица дополнительной информации */
        @SerializedName("ET_ADDINFO")
        val additionalInfoList: List<AdditionalInfo>,
        /** Таблица МХ */
        @SerializedName("ET_PLACES")
        val places: List<Place>,
        /** Таблица поставщиков */
        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>,
        /** Таблица остатков по складам */
        @SerializedName("ET_STOCKS")
        val stocks: List<Stock>,
        /** Таблица результаты проверки */
        @SerializedName("ET_CHECK_RESULT")
        val checkResults: List<CheckResult>,
        /** Справочные данные товара */
        @SerializedName("ET_MATERIALS")
        val productsInfo: List<ProductInfo>,
        /** Таблица марок задания */
        @SerializedName("ET_TASK_MARK")
        val marks: List<Mark>,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)