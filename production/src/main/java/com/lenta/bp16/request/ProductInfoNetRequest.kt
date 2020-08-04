package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class ProductInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ProductInfoResult, ProductInfoParams> {

    override suspend fun run(params: ProductInfoParams): Either<Failure, ProductInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_45_V001", params, ProductInfoStatus::class.java)
    }

}

data class ProductInfoParams(
        @SerializedName("IT_MATNR_LIST")
        val matnr: List<Product>,
        @SerializedName("IT_EAN_LIST")
        val ean: List<Ean>
)

class ProductInfoStatus : ObjectRawStatus<ProductInfoResult>()

data class ProductInfoResult(
        @SerializedName("ET_MATNR_LIST")
        val product: List<ProductInfo>,
        @SerializedName("ET_SET_LIST")
        val set: List<SetInfo>,
        @SerializedName("ET_PROD_TEXT")
        val producers: List<ProducerInfo>
)