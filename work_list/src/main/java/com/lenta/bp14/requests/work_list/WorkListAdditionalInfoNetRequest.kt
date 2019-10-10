package com.lenta.bp14.requests.work_list

import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.work_list.AdditionalGoodInfo
import com.lenta.bp14.models.work_list.Provider
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.requests.EanParam
import com.lenta.bp14.requests.MatNrParam
import com.lenta.bp14.requests.ProductInfoNetRequest
import com.lenta.bp14.requests.ProductInfoParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class WorkListAdditionalInfoNetRequest
@Inject constructor(private val productInfoNetRequest: ProductInfoNetRequest) : IWorkListAdditionalInfoNetRequest {

    override suspend fun run(params: WorkListAdditionalInfoParams): Either<Failure, AdditionalGoodInfo> {
        return productInfoNetRequest(params = params.toCommonParams()).map {
            val additional = it.additionalInfoList[0]
            val providers = it.suppliers
            val stocks = it.stocks

            var storagePlaces = ""
            val lastPlace = it.places.last()
            it.places.map { place ->
                storagePlaces += place
                if (place != lastPlace) storagePlaces += ", "
            }

            AdditionalGoodInfo(
                    storagePlaces = storagePlaces,
                    minStock = additional.minStock,
                    inventory = additional.lastInv,
                    arrival = additional.planDelivery,
                    commonPrice = additional.price1.toDoubleOrNull() ?: 0.0,
                    discountPrice = additional.price2.toDoubleOrNull() ?: 0.0,
                    promoName = additional.promoText1,
                    promoPeriod = additional.promoText2,
                    providers = providers.map { provider ->
                        Provider(
                                code = provider.lifnr,
                                name = provider.lifnrName,
                                period = provider.periodAct
                        )
                    },
                    stocks = stocks.map { stock ->
                        Stock(
                                storage = stock.lgort,
                                quantity = stock.stock
                        )
                    }
            )
        }
    }

}

interface IWorkListAdditionalInfoNetRequest : UseCase<AdditionalGoodInfo, WorkListAdditionalInfoParams>

private fun WorkListAdditionalInfoParams.toCommonParams(): ProductInfoParams {
    require((!ean.isNullOrBlank() xor !matNr.isNullOrBlank()))
    return ProductInfoParams(
            taskType = AppTaskTypes.WorkList.taskType,
            withProductInfo = "",
            withAdditionalInf = "X",
            tkNumber = tkNumber,
            eanList = if (ean == null) emptyList() else listOf(EanParam(ean)),
            matNrList = if (matNr == null) emptyList() else listOf(MatNrParam(matNr))
    )
}

data class WorkListAdditionalInfoParams(
        val tkNumber: String,
        val ean: String?,
        val matNr: String?
)