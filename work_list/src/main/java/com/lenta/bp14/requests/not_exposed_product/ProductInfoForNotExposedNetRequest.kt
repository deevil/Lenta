package com.lenta.bp14.requests.not_exposed_product

import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.requests.*
import com.lenta.bp14.requests.pojo.ProductInfo
import com.lenta.bp14.requests.pojo.Stock
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Uom
import com.mobrun.plugin.api.HyperHive
import java.util.*
import javax.inject.Inject

class ProductInfoForNotExposedNetRequest
@Inject constructor(private val productInfoNetRequest: ProductInfoNetRequest, hyperHive: HyperHive) : IProductInfoForNotExposedNetRequest {

    val zmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    override suspend fun run(params: NotExposedInfoRequestParams): Either<Failure, GoodInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            GoodInfo(
                    productInfo = it.productsInfo[0],
                    stocks = it.stocks,
                    uom = zmpUtz07V001.getUomInfo(it.productsInfo[0].bUom)?.let { etUom ->
                        Uom(
                                code = etUom.uom,
                                name = etUom.name.toLowerCase(Locale.getDefault())
                        )
                    }
            )
        }
    }

}

interface IProductInfoForNotExposedNetRequest : UseCase<GoodInfo, NotExposedInfoRequestParams>

private fun NotExposedInfoRequestParams.toCommonParams(): ProductInfoParams {
    require((!ean.isNullOrBlank() xor !matNr.isNullOrBlank()))
    return ProductInfoParams(
            taskType = AppTaskTypes.NotExposedProducts.taskType,
            withProductInfo = "X",
            withAdditionalInf = "X",
            tkNumber = tkNumber,
            eanList = if (ean == null) emptyList() else listOf(EanParam(ean)),
            matNrList = if (matNr == null) emptyList() else listOf(MatNrParam(matNr))
    )
}


data class GoodInfo(
        val productInfo: ProductInfo,
        val stocks: List<Stock>,
        val uom: Uom?
)

data class NotExposedInfoRequestParams(
        val tkNumber: String,
        val ean: String?,
        val matNr: String?
)

