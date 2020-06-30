package com.lenta.bp16.request

import com.google.gson.annotations.SerializedName
import com.lenta.bp16.request.pojo.PackCode
import com.lenta.bp16.request.pojo.RetCode
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class EndDefrostingNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<EndDefrostingResult, EndDefrostingParams> {

    override suspend fun run(params: EndDefrostingParams): Either<Failure, EndDefrostingResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_05_V001", params, EndDefrostingStatus::class.java)
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

data class EndDefrostingParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,
        /** Список тар */
        @SerializedName("IT_CONT_LIST")
        val packCodes: List<PackCode>
)

class EndDefrostingStatus : ObjectRawStatus<EndDefrostingResult>()

data class EndDefrostingResult(
        /** ??? */
        @SerializedName("IS_AUTOFIX")
        val isAutofix: String,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)