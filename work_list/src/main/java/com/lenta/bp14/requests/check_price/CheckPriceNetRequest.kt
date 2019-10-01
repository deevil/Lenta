package com.lenta.bp14.requests.check_price

import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.requests.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class CheckPriceNetRequest
@Inject constructor(private val productInfoNetRequest: ProductInfoNetRequest) : UseCase<ActualPriceInfo, CheckPriceRequestParams>() {

    override suspend fun run(params: CheckPriceRequestParams): Either<Failure, ActualPriceInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            val productInfo = it.productsInfo[0]
            val price = it.prices[0]
            ActualPriceInfo(
                    matNumber = productInfo.matNr,
                    productName = productInfo.name,
                    price1 = price.price1.toFloatOrNull(),
                    price2 = price.price2.toFloatOrNull(),
                    price3 = price.price3.toFloatOrNull(),
                    price4 = price.price4.toFloatOrNull()
            )
        }
    }

}

private fun CheckPriceRequestParams.toCommonParams(): ProductInfoParams {
    require((!ean.isNullOrBlank() xor !matNr.isNullOrBlank()))
    return ProductInfoParams(
            taskType = TaskTypes.CheckPrice.taskType.taskType,
            withProductInfo = "X",
            withAdditionalInf = "X",
            tkNumber = tkNumber,
            eanList = if (ean == null) emptyList() else listOf(EanParam(ean)),
            matNrList = if (matNr == null) emptyList() else listOf(MatNrParam(matNr))
    )
}



data class CheckPriceRequestParams(
        val tkNumber: String,
        val ean: String?,
        val matNr: String?
)

