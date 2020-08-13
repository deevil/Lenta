package com.lenta.bp18.request.network

import com.lenta.bp18.request.model.params.MarketInfoParams
import com.lenta.bp18.request.model.result.MarketInfoResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class MarketOverIPRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<MarketInfoResult, MarketInfoParams> {

    override suspend fun run(params: MarketInfoParams): Either<Failure, MarketInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_89_V001", params, MarketOverIPStatus::class.java)
    }
}

class MarketOverIPStatus : ObjectRawStatus<MarketInfoResult>()