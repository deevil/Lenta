package com.lenta.bp14.requests.not_exposed_product

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.CheckPlace
import com.lenta.bp14.requests.pojo.ProductInfo
import com.lenta.bp14.requests.pojo.Stock
import com.lenta.bp14.requests.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class NotExposedTaskInfoNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : INotExposedTaskInfoNetRequest {
    override suspend fun run(params: NotExposedTaskInfoParams): Either<Failure, NotExposedTaskInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_05_V001", params, NotExposedTaskInfoStatus::class.java)
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

interface INotExposedTaskInfoNetRequest : UseCase<NotExposedTaskInfoResult, NotExposedTaskInfoParams>

data class NotExposedTaskInfoParams(
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


class NotExposedTaskInfoStatus : ObjectRawStatus<NotExposedTaskInfoResult>()

data class NotExposedTaskInfoResult(
        @SerializedName("ET_TASK_POS")
        val positions: List<RetCode>,
        @SerializedName("ET_STOCKS")
        val stocks: List<Stock>,
        @SerializedName("ET_MATERIALS")
        val productsInfo: List<ProductInfo>,
        @SerializedName("ET_CHECK_PLACE")
        val checkPlaces: List<CheckPlace>,
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)

