package com.lenta.bp12.request

import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestParams
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestStatus
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/**
 * 3.1.1.15	Таблица 17. Параметры ФМ ZMP_UTZ_WOB_07_V001 «Получение данных по марке/блоку/коробке/товару из ГМ»
 * @see MarkCartonBoxGoodInfoNetRequestParams
 * @see MarkCartonBoxGoodInfoNetRequestResult
 */
class MarkCartonBoxGoodInfoNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<MarkCartonBoxGoodInfoNetRequestResult, MarkCartonBoxGoodInfoNetRequestParams> {

    override suspend fun run(params: MarkCartonBoxGoodInfoNetRequestParams):
            Either<Failure, MarkCartonBoxGoodInfoNetRequestResult> {
        return fmpRequestsHelper.restRequest(
                RESOURCE_NAME, params, MarkCartonBoxGoodInfoNetRequestStatus::class.java
        )
    }

    companion object {
        const val RESOURCE_NAME = "ZMP_UTZ_WOB_07_V001"
    }
}


