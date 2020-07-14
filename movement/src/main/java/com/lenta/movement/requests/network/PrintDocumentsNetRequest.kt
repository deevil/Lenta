package com.lenta.movement.requests.network

import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.requests.network.models.printDocuments.PrintDocumentsParams
import com.lenta.movement.requests.network.models.printDocuments.PrintDocumentsResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.orIfNull
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
                result.b.errorTxt?.let{ errorText ->
                    Either.Left(InfoFailure(errorText))
                }.orIfNull { Either.Left(Failure.ServerError) }

            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_11_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class PrintDocumentsStatus : ObjectRawStatus<PrintDocumentsResult>()



