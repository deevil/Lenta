package com.lenta.movement.requests.network

import com.lenta.movement.requests.network.models.obtainingTaskComposition.TaskCompositionParams
import com.lenta.movement.requests.network.models.obtainingTaskComposition.TaskCompositionResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_03_V001	«Получение состава задания» */
class ObtainingTaskComposition @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskCompositionResult, TaskCompositionParams> {

    override suspend fun run(params: TaskCompositionParams): Either<Failure, TaskCompositionResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = TaskStatus::class.java
        )
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_03_V001"
    }
}

class TaskStatus : ObjectRawStatus<TaskCompositionResult>()



