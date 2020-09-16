package com.lenta.bp14.requests.check_price

import com.lenta.bp14.models.check_price.ActualPriceInfo
import com.lenta.bp14.models.check_price.GoodOptions
import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.requests.EanParam
import com.lenta.bp14.requests.MatNrParam
import com.lenta.bp14.requests.ProductInfoNetRequest
import com.lenta.bp14.requests.ProductInfoParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.toNullIfEmpty
import javax.inject.Inject

class CheckPriceNetRequest
@Inject constructor(private val productInfoNetRequest: ProductInfoNetRequest) : ICheckPriceNetRequest {

    override suspend fun run(params: CheckPriceRequestParams): Either<Failure, ActualPriceInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            val productInfo = it.productsInfo[0]
            val price = it.prices[0]
            ActualPriceInfo(
                    matNumber = productInfo.matNr,
                    productName = productInfo.name,
                    price1 = price.price1,
                    price2 = price.price2.toNullIfEmpty(),
                    price3 = price.price3.toNullIfEmpty(),
                    price4 = price.price4.toNullIfEmpty(),
                    options = GoodOptions(
                            matrixType = getMatrixType(productInfo.matrixType),
                            section = if (productInfo.sectionNumber.isNotEmpty()) productInfo.sectionNumber else "91",
                            goodType = getGoodType(
                                    alcohol = productInfo.isAlco,
                                    excise = productInfo.isExcise,
                                    marked = productInfo.isMarked,
                                    rusWine = productInfo.isRusWine),
                            healthFood = productInfo.isHealthyFood.isSapTrue(),
                            novelty = productInfo.isNew.isSapTrue()
                    )
            )
        }
    }

}

interface ICheckPriceNetRequest : UseCase<ActualPriceInfo, CheckPriceRequestParams>

private fun CheckPriceRequestParams.toCommonParams(): ProductInfoParams {
    require((!ean.isNullOrBlank() xor !matNr.isNullOrBlank()))
    return ProductInfoParams(
            taskType = AppTaskTypes.CheckPrice.taskType,
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

