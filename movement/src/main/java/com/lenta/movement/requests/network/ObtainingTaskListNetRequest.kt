package com.lenta.movement.requests.network

import com.lenta.movement.requests.network.models.task_list.TaskListParams
import com.lenta.movement.requests.network.models.task_list.TaskListResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_02_V001	«Загрузка списка заданий» */
class ObtainingTaskListNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<TaskListResult, TaskListParams> {

    override suspend fun run(params: TaskListParams): Either<Failure, TaskListResult> {
        return fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = TaskListStatus::class.java
        )
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_02_V001"
        const val UPDATE_MODE = "1"
        const val SEARCH_MODE = "2"
    }
}

class TaskListStatus : ObjectRawStatus<TaskListResult>()



