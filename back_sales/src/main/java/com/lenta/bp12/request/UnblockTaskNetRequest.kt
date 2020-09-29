package com.lenta.bp12.request

import com.google.gson.annotations.SerializedName
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.requests.SapResponse
import javax.inject.Inject

class UnblockTaskNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<UnblockTaskResult, UnblockTaskParams> {

    override suspend fun run(params: UnblockTaskParams): Either<Failure, UnblockTaskResult> {
        return fmpRequestsHelper.restRequest("ZMP_UTZ_BKS_06_V001", params, UnblockTaskStatus::class.java)
    }

}

data class UnblockTaskParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String,
        /** Табельный номер */
        @SerializedName("IV_PERNR")
        val userNumber: String,
        /** IP адрес ТСД */
        @SerializedName("IV_IP_PDA")
        val deviceIp: String
)

class UnblockTaskStatus : ObjectRawStatus<UnblockTaskResult>()

data class UnblockTaskResult(
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse