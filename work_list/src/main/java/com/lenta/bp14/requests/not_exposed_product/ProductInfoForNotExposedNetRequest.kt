package com.lenta.bp14.requests.not_exposed_product

import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.requests.*
import com.lenta.bp14.requests.pojo.ProductInfo
import com.lenta.bp14.requests.pojo.Stock
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
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

    val units by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения

    override suspend fun run(params: NotExposedInfoRequestParams): Either<Failure, GoodInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            //TODO зачем нужен этот код? Закоментировал до выяснения
            //val unitsCode = if (it.productsInfo[0].bUom == Uom.G.code) Uom.KG.code else it.productsInfo[0].bUom
            val unitsCode = it.productsInfo[0].bUom
            val unitsName = units.getUnitName(unitsCode)?.toLowerCase(Locale.getDefault())

            GoodInfo(
                    productInfo = it.productsInfo[0],
                    stocks = it.stocks,
                    uom = Uom(
                            code = unitsCode,
                            name = unitsName ?: ""
                    )
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

data class GoodInfoWithQuantity(
        val goodInfo: GoodInfo,
        val quantity: Double
)


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

