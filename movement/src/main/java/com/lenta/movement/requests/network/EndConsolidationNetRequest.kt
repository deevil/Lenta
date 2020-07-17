package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.endConsolidation.EndConsolidationParams
import com.lenta.movement.requests.network.models.endConsolidation.EndConsolidationResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_10_V001	«Окончание консолидации» */
class EndConsolidationNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<EndConsolidationResult, EndConsolidationParams> {

    override suspend fun run(params: EndConsolidationParams): Either<Failure, EndConsolidationResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = EndConsolidationStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_10_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class EndConsolidationStatus : ObjectRawStatus<EndConsolidationResult>()


