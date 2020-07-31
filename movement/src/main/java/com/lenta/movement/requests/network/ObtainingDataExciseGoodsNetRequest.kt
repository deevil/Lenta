package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest.ExciseGoodsParams
import com.lenta.movement.requests.network.models.obtainingDataExciseGoodsNetRequest.ExciseGoodsRestInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** "ZMP_UTZ_100_V001" «Получение данных по акцизному товару»*/

class ObtainingDataExciseGoodsNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ExciseGoodsRestInfo, ExciseGoodsParams> {

    override suspend fun run(params: ExciseGoodsParams): Either<Failure, ExciseGoodsRestInfo> {
        val result = fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = ExciseGoodsStatus::class.java
        )
        return if (result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
            Either.Left(InfoFailure(result.b.errorTxt.orEmpty()))
        } else result

    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_100_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class ExciseGoodsStatus : ObjectRawStatus<ExciseGoodsRestInfo>()




