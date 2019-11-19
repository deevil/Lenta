package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class UnblockTaskNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<UnblockTaskResult, UnblockTaskParams> {

    override suspend fun run(params: UnblockTaskParams): Either<Failure, UnblockTaskResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_06_V001", params, UnblockTaskStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode == 1
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }

}

data class UnblockTaskParams(
        /** Номер ЕО */
        @SerializedName("EXIDV")
        val puNumber: String
)

class UnblockTaskStatus : ObjectRawStatus<UnblockTaskResult>()

data class UnblockTaskResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)