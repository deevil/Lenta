package com.lenta.movement.requests.network

import com.lenta.movement.models.Task
import com.lenta.movement.requests.network.models.saveTask.SaveTaskParams
import com.lenta.movement.requests.network.models.saveTask.SaveTaskResult
import com.lenta.movement.requests.network.models.toTask
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

class SaveTaskNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<Task, SaveTaskParams> {

    override suspend fun run(params: SaveTaskParams): Either<Failure, Task> {
        val result = fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = SaveTaskStatus::class.java
        )
        return when (result) {
            is Either.Left -> result
            is Either.Right -> {
                val savedTask = result.b.tasks.firstOrNull()

                when {
                    savedTask == null -> result.left(Failure.ServerError)
                    savedTask.errorText.isNotEmpty() -> result.left(Failure.SapError(savedTask.errorText))
                    else -> result.right(savedTask.toTask())
                }
            }
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_04_V001"
    }
}

class SaveTaskStatus : ObjectRawStatus<SaveTaskResult>()