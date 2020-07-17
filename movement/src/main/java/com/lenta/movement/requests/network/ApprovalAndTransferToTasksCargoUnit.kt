package com.lenta.movement.requests.network

import com.google.gson.annotations.SerializedName
import com.lenta.movement.exception.InfoFailure
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType
import com.lenta.movement.requests.network.models.approvalAndTransferToTasksCargoUnit.ApprovalAndTransferToTasksCargoUnitParams
import com.lenta.movement.requests.network.models.approvalAndTransferToTasksCargoUnit.ApprovalAndTransferToTasksCargoUnitResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_13_V001 «Одобрение и передача на ГЗ задания» */
class ApprovalAndTransferToTasksCargoUnit @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<ApprovalAndTransferToTasksCargoUnitResult, ApprovalAndTransferToTasksCargoUnitParams> {

    override suspend fun run(params: ApprovalAndTransferToTasksCargoUnitParams): Either<Failure, ApprovalAndTransferToTasksCargoUnitResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = ApprovalAndTransferToTasksCargoUnitStatus::class.java
        ).let { result ->
            if (result is Either.Right && result.b.retCode != NON_FAILURE_RET_CODE) {
                result.b.errorTxt?.let { errorTxt ->
                    Either.Left(InfoFailure(errorTxt))
                } ?: Either.Left(Failure.ServerError)
            } else result
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_13_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class ApprovalAndTransferToTasksCargoUnitStatus : ObjectRawStatus<ApprovalAndTransferToTasksCargoUnitResult>()