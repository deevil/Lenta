package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** Документы для печати ZMP_UTZ_MVM_12_V001 */
class DocumentsToPrintNetRequest@Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<DocumentsToPrintResult, DocumentsToPrintParams> {

    override suspend fun run(params: DocumentsToPrintParams): Either<Failure, DocumentsToPrintResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = DocumentsToPrintStatus::class.java
        ).let { result ->
            if(result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                Either.Left(InfoFailure(result.b.errorTxt))
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_12_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class DocumentsToPrintStatus : ObjectRawStatus<DocumentsToPrintResult>()

data class DocumentsToPrintParams(
        /** Номер задания */
        @SerializedName("IV_TASK_NUM")
        val taskNumber: String
)

data class DocumentsToPrintResult(

        /** Список документов для печати */
        @SerializedName("ET_DOC_PRINT")
        val docList: String,

        /** Код возврата */
        @SerializedName("EV_RETCODE")
        val retCode: String,

        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        val errorTxt: String
)