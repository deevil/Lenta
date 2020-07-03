package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationParams
import com.lenta.movement.requests.network.models.startConsolidation.StartConsolidationResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_08_V001	«Начало консолидации» */
class StartConsolidation @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<StartConsolidationResult, StartConsolidationParams> {

    override suspend fun run(params: StartConsolidationParams): Either<Failure, StartConsolidationResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = StartConsolidationStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_08_V001"
        private const val NON_FAILURE_RET_CODE = "0"

        const val MODE_GET_TASK_COMP_CODE = 1
        const val MODE_GET_TASK_COMP_WITH_BLOCK_CODE = 2
    }
}

class StartConsolidationStatus : ObjectRawStatus<StartConsolidationResult>()



