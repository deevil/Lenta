package com.lenta.bp12.request

import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListParams
import com.lenta.bp12.request.pojo.print_pallet_list.PrintPalletListResult
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
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<PrintPalletListResult, PrintPalletListParams> {

    override suspend fun run(params: PrintPalletListParams): Either<Failure, PrintPalletListResult> {
        val result =  fmpRequestsHelper.restRequest(RESOURCE_NAME, params, PrintPalletListStatus::class.java)
        return if (result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
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

