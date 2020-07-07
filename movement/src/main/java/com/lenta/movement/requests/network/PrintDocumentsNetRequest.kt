package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** Печать документов ZMP_UTZ_MVM_11_V001 */
class PrintDocumentsNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<PrintDocumentsResult, PrintDocumentsParams> {

    override suspend fun run(params: PrintDocumentsParams): Either<Failure, PrintDocumentsResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = PrintDocumentsStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_11_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class PrintDocumentsStatus : ObjectRawStatus<PrintDocumentsResult>()

data class PrintDocumentsParams(
        /** IP адрес ТСД */
        @SerializedName("IV_IP")
        val deviceIp: String,

        /** Имя принтера */
        @SerializedName("IV_PRINTERNAME")
        val printerName: String
        )

data class PrintDocumentsResult(
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
)