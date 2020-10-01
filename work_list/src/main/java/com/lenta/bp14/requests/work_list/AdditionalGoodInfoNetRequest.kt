package com.lenta.bp14.requests.work_list

import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.work_list.AdditionalGoodInfo
import com.lenta.bp14.models.work_list.Provider
import com.lenta.bp14.requests.*
import com.lenta.bp14.requests.base.BaseGoodInfoNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class AdditionalGoodInfoNetRequest
@Inject constructor(
        private val productInfoNetRequest: ProductInfoNetRequest
) : BaseGoodInfoNetRequest(), IAdditionalGoodInfoNetRequest {

    override suspend fun run(params: AdditionalGoodInfoParams): Either<Failure, AdditionalGoodInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            val additional = it.additionalInfoList[0]
            val providers = it.suppliers

            var storagePlaces = ""
            val lastPlace = it.places.lastOrNull()
            it.places.map { place ->
                storagePlaces += place.placeCode
                if (place != lastPlace) storagePlaces += ", "
            }

            val zParts = it.zParts.mapToZPartList()
            AdditionalGoodInfo(
                    storagePlaces = storagePlaces,
                    minStock = additional.minStock,
                    inventory = additional.lastInv,
                    arrival = additional.planDelivery,
                    commonPrice = additional.price1.toDoubleOrZero(),
                    discountPrice = additional.price2.toDoubleOrZero(),
                    promoName = additional.promoText1,
                    promoPeriod = additional.promoText2,
                    providers = providers.map { provider ->
                        Provider(
                                code = provider.lifnr,
                                name = provider.lifnrName,
                                period = provider.periodAct
                        )
                    },
                    stocks = it.extractStocks(zParts),
                    zParts = zParts
            )
        }
    }


}

interface IAdditionalGoodInfoNetRequest : UseCase<AdditionalGoodInfo, AdditionalGoodInfoParams>

private fun AdditionalGoodInfoParams.toCommonParams(): ProductInfoParams {
    require(!ean.isNullOrBlank() || !matNr.isNullOrBlank())
    return ProductInfoParams(
            taskType = AppTaskTypes.WorkList.taskType,
            withProductInfo = "",
            withAdditionalInf = "X",
            tkNumber = tkNumber,
            eanList = if (ean == null) emptyList() else listOf(EanParam(ean)),
            matNrList = if (matNr == null) emptyList() else listOf(MatNrParam(matNr))
    )
}

data class AdditionalGoodInfoParams(
        val tkNumber: String,
        val ean: String?,
        val matNr: String?
)