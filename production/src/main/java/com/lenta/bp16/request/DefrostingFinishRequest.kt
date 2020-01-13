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

class DefrostingFinishRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<DefrostingFinishResult, DefrostingFinishParams> {

    override suspend fun run(params: DefrostingFinishParams): Either<Failure, DefrostingFinishResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_05_V001", params, DefrostingFinishStatus::class.java)
                .rightToLeft(
                        fnRtoL = { result ->
                            result.retCodes.firstOrNull { retCode ->
                                retCode.retCode != 0
                            }?.let { retCode ->
                                return@rightToLeft Failure.SapError(retCode.errorText)
                            }
                        }
                )
    }

}

data class DefrostingFinishParams(
        /** IP ТСД */
        @SerializedName("IV_IP")
        val ip: String,
        /** Список тар */
        @SerializedName("IT_CONT_LIST")
        val containers: List<ContainerInfo>

)

data class ContainerInfo(
        @SerializedName("CODE_CONT")
        val marketNumber: String
)

class DefrostingFinishStatus : ObjectRawStatus<DefrostingFinishResult>()

data class DefrostingFinishResult(
        /** Результаты запросов */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)