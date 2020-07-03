package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.consolidation.ConsolidationParams
import com.lenta.movement.requests.network.models.consolidation.ConsolidationResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_09_V001	«Консолидация ЕО\ГЕ» */
class Consolidation @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ConsolidationResult, ConsolidationParams> {

    override suspend fun run(params: ConsolidationParams): Either<Failure, ConsolidationResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = ConsolidationStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_09_V001"
        private const val NON_FAILURE_RET_CODE = "0"

        const val CONSOLIDATION_EO_IN_GE_MODE = "01" // Кнопка обработать (отправляешь ЕО)
        const val SEPARATION_GE_TO_EO_MODE = "02" // Кнопка разобрать (отправляешь ГЕ)
        const val ADD_EO_IN_GE_MODE = "03" // В карточке ГЕ, если хочешь добавить ЕО (напр отсканировать)
        const val EXCLUDE_EO_FROM_GE_MODE = "04" // В карточке ГЕ, если хочешь исключить ЕО
    }
}

class ConsolidationStatus : ObjectRawStatus<ConsolidationResult>()



