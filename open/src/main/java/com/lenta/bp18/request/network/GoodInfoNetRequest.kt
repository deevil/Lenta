package com.lenta.bp18.request.network

import com.lenta.bp18.request.model.params.GoodInfoParams
import com.lenta.bp18.request.model.result.GoodInfoResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class GoodInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<GoodInfoResult, GoodInfoParams> {

    override suspend fun run(params: GoodInfoParams): Either<Failure, GoodInfoResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_OPP_01_V001", params, GoodInfoStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCode.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }
}

class GoodInfoStatus : ObjectRawStatus<GoodInfoResult>()