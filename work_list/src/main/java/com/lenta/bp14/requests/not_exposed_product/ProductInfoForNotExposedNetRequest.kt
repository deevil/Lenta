package com.lenta.bp14.requests.not_exposed_product

import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.requests.EanParam
import com.lenta.bp14.requests.MatNrParam
import com.lenta.bp14.requests.ProductInfoNetRequest
import com.lenta.bp14.requests.ProductInfoParams
import com.lenta.bp14.requests.base.BaseGoodInfoNetRequest
import com.lenta.bp14.requests.pojo.ProductInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getUnitName
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.mobrun.plugin.api.HyperHive
import java.util.*
import javax.inject.Inject

class ProductInfoForNotExposedNetRequest
@Inject constructor(
        private val productInfoNetRequest: ProductInfoNetRequest,
        hyperHive: HyperHive
) : BaseGoodInfoNetRequest(), IProductInfoForNotExposedNetRequest {

    val units by lazy { ZmpUtz07V001(hyperHive) } // Единицы измерения

    override suspend fun run(params: NotExposedInfoRequestParams): Either<Failure, GoodInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            val defaultUnits = Uom(code = it.productsInfo[0].bUom, name = getUnitsName(it.productsInfo[0].bUom))
            val units = if (defaultUnits == Uom.G) Uom.KG else defaultUnits

            val zParts = it.zParts.mapToZPartList()
            GoodInfo(
                    productInfo = it.productsInfo[0],
                    stocks = it.extractStocks(zParts),
                    defaultUnits = defaultUnits,
                    units = units,
                    zParts = zParts
            )
        }
    }

    private fun getUnitsName(code: String?): String {
        return units.getUnitName(code)?.toLowerCase(Locale.getDefault()).orEmpty()
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
        val defaultUnits: Uom?,
        val units: Uom?,
        val zParts: List<ZPart>
) {
    val hasZParts: Boolean
        get() = zParts.isNotEmpty()
}

data class NotExposedInfoRequestParams(
        val tkNumber: String,
        val ean: String?,
        val matNr: String?
)