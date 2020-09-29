package com.lenta.bp14.requests

import com.google.gson.annotations.SerializedName
import com.lenta.bp14.requests.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class ProductInfoNetRequest
@Inject constructor(private val fmpRequestsHelper: FmpRequestsHelper) : UseCase<ProductInfoResult, ProductInfoParams> {
    override suspend fun run(params: ProductInfoParams): Either<Failure, ProductInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_WKL_11_V001", params, ProductInfoStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            if (params.withProductInfo.isSapTrue() && result.productsInfo.isNullOrEmpty()) {
                                return@rightToLeft Failure.GoodNotFound
                            } else {
                                result.retCodes.firstOrNull { it.retCode == 1 }?.apply {
                                    return@rightToLeft Failure.SapError(this.errorText)
                                }
                                null
                            }
                        }
                )
    }

}

data class ProductInfoParams(
        @SerializedName("IV_MATNR_DATA_FLAG")
        val withProductInfo: String,
        @SerializedName("IV_RBSINFO_FLAG")
        val withAdditionalInf: String,
        @SerializedName("IV_WERKS")
        val tkNumber: String,
        @SerializedName("IV_TASK_TYPE")
        val taskType: String,
        @SerializedName("IT_EAN_LIST")
        val eanList: List<EanParam>? = null,
        @SerializedName("IT_MATNR_LIST")
        val matNrList: List<MatNrParam>? = null
)

data class EanParam(
        @SerializedName("EAN")
        val ean: String
)

data class MatNrParam(
        @SerializedName("MATNR")
        val matNr: String
)


class ProductInfoStatus : ObjectRawStatus<ProductInfoResult>()

data class ProductInfoResult(
        @SerializedName("ET_ADDINFO")
        val additionalInfoList: List<AdditionalInfo>,
        @SerializedName("ET_LIFNR")
        val suppliers: List<Supplier>,
        @SerializedName("ET_MATERIALS")
        // Информация по товарам
        val productsInfo: List<ProductInfo>,
        @SerializedName("ET_PLACES")
        val places: List<Place>,
        @SerializedName("ET_PRICE")
        val prices: List<Price>,
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>,
        @SerializedName("ET_STOCKS")
        val stocks: List<Stock>,
        @SerializedName("ET_ZPARTS")
        val zParts: List<ZPartDTO>
)