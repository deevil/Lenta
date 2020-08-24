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

class EndProcessingNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<EndProcessingResult, EndProcessingParams> {

    override suspend fun run(params: EndProcessingParams): Either<Failure, EndProcessingResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_PRO_08_V001", params, EndProcessingStatus::class.java)
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

data class EndProcessingParams(
        /** Номер объекта */
        @SerializedName("IV_OBJ_CODE")
        val taskNumber: String,
        /** Режим обработки: 1 - обработка ЕО, 2 - обработка ВП */
        @SerializedName("IV_MODE")
        val taskType: Int
)

class EndProcessingStatus : ObjectRawStatus<EndProcessingResult>()

data class EndProcessingResult(
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        val retCodes: List<RetCode>
)