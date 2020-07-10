package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintParams
import com.lenta.movement.requests.network.models.documentsToPrint.DocumentsToPrintResult
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
                val errorText = result.b.errorTxt
                errorText?.let {
                    Either.Left(InfoFailure(it))
                } ?: Either.Left(Failure.ServerError)
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_12_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class DocumentsToPrintStatus : ObjectRawStatus<DocumentsToPrintResult>()



