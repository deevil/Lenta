package com.lenta.bp12.request

import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.good_info.GoodInfoStatus
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/**
 * Получение данных товара по ШК\SAP-коду "ZMP_UTZ_BKS_05_V001"
 * @see GoodInfoParams
 * @see GoodInfoResult
 */
class GoodInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<GoodInfoResult, GoodInfoParams> {

    override suspend fun run(params: GoodInfoParams): Either<Failure, GoodInfoResult> {
        return fmpRequestsHelper.restRequest(RESOURCE_NAME, params, GoodInfoStatus::class.java)
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_BKS_05_V001"
    }
}