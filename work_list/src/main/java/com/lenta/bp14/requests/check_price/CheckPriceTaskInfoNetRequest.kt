package com.lenta.bp14.requests.check_price

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.Price
import com.lenta.bp14.requests.ProductInfo
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class CheckPriceTaskInfoNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : ICheckPriceTaskInfoNetRequest {
    override suspend fun run(params: CheckPriceTaskInfoParams): Either<Failure, CheckPriceTaskInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_04_V001", params, CheckPriceTaskInfoStatus::class.java)
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

interface ICheckPriceTaskInfoNetRequest : UseCase<CheckPriceTaskInfoResult, CheckPriceTaskInfoParams>

data class CheckPriceTaskInfoParams(
        @SerializedName("IV_IP")
        val ip: String,
        @SerializedName("IV_MATNR_DATA_FLG")
        val withProductInfo: String,
        /**
         * Режим работы:
         * «1» - получение состава задания
         * «2» - получение состава задания с переблокировкой
         */
        @SerializedName("IV_MODE")
        val mode: String,
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)


class CheckPriceTaskInfoStatus : ObjectRawStatus<CheckPriceTaskInfoResult>()

data class CheckPriceTaskInfoResult(
        @SerializedName("ET_CHECK_PRICE")
        val checkPrices: List<CheckResult>,
        @SerializedName("ET_MATERIALS")
        val productsInfo: List<ProductInfo>,
        @SerializedName("ET_PRICE")
        val prices: List<Price>,
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>,
        @SerializedName("ET_TASK_POS")
        val positions: List<RetCode>
)

