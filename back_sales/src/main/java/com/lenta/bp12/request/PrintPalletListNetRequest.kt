package com.lenta.bp12.request

import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParams
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsBasket
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParamsGood
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** "ZMP_UTZ_BKS_07_V001"
 * Печать паллетной ведомости
 */
class PrintPalletListNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper,
        private val sessionInfo: ISessionInfo,
        private val resource: IResourceManager
) : UseCase<PrintPalletListResult, Triple<List<Basket>, Boolean, Boolean>> {

    override suspend fun run(params: Triple<List<Basket>, Boolean, Boolean>): Either<Failure, PrintPalletListResult> {

        val goodListRest = params.first.flatMap { basket ->
                val distinctGoods = basket.goods.keys
                distinctGoods.map { good ->
                    val quantity = basket.goods[good]
                    PrintPalletListParamsGood(
                            materialNumber = good.material,
                            basketNumber = basket.index.toString(),
                            quantity = quantity.toString(),
                            uom = good.commonUnits.code
                    )
                }
            }

        val isDivBySection = params.second
        val isWholeSale = params.third

        val basketListRest = params.first.map {
                val description = it.getDescription(isDivBySection, isWholeSale)
                PrintPalletListParamsBasket(
                        number = it.index.toString(),
                        description = description,
                        section = it.section.orEmpty()
                )
            }

        val sendParams =  PrintPalletListParams(
                userNumber = sessionInfo.personnelNumber.orEmpty(),
                deviceIp = resource.deviceIp,
                baskets = basketListRest,
                goods = goodListRest
        )

        val result =  fmpRequestsHelper.restRequest(
                RESOURCE_NAME,
                sendParams,
                PrintPalletListStatus::class.java
        )

        return if (result is Either.Right && result.b.retCode != null && result.b.retCode != NON_FAILURE_RET_CODE) {
            Either.Left(Failure.SapError(result.b.errorText.orEmpty()))
        } else {
            result
        }
    }
    companion object {
        private const val NON_FAILURE_RET_CODE = 0
        private const val RESOURCE_NAME = "ZMP_UTZ_BKS_07_V001"
    }
}

class PrintPalletListStatus : ObjectRawStatus<PrintPalletListResult>()

