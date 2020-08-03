package com.lenta.movement.requests.network

import com.lenta.movement.models.Task
import com.lenta.movement.requests.network.models.saveTask.SaveTaskParams
import com.lenta.movement.requests.network.models.saveTask.SaveTaskResult
import com.lenta.movement.requests.network.models.saveTask.SaveTaskResultTask
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.requests.FmpRequestsHelper
import javax.inject.Inject

/** ZMP_UTZ_MVM_04_V001 «Сохранение в SAP данных задания»*/
class SaveTaskNetRequest @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : UseCase<SaveTaskResultTask, SaveTaskParams> {

    override suspend fun run(params: SaveTaskParams): Either<Failure, SaveTaskResultTask> {
        val result = fmpRequestsHelper.restRequest(
                resourceName = RESOURCE_NAME,
                data = params,
                clazz = SaveTaskStatus::class.java
        )

        return when (result) {
            is Either.Left -> result
            is Either.Right -> {
                val savedTask = result.b.tasks?.firstOrNull()

                when {
                    savedTask == null -> result.left(Failure.ServerError)
                    savedTask.errorText.isNullOrEmpty().not() -> result.left(Failure.SapError(savedTask.errorText.orEmpty()))
                    //Если на складе нехватает товаров, то сервер все равно возвращает задание в состоянии Посчитано,
                    //но вместе с ошибкой, поэтому вернем задание, но его поле ошибки (оно все равно пустое в этот момент)
                    //перезапишем ошибкой сервера и проверим в TaskViewModel
                    result.b.retCode != NON_FAILURE_RET_CODE && savedTask.currentStatusCode == Task.Status.COUNTED_CODE ->{
                        val errorText = result.b.errorTxt.orEmpty()
                        savedTask.errorText = errorText
                        result.right(savedTask)
                    }
                    result.b.retCode != NON_FAILURE_RET_CODE && savedTask.currentStatusCode != Task.Status.COUNTED_CODE ->
                        result.left(Failure.SapError(result.b.errorTxt.orEmpty()))
                    else -> result.right(savedTask)
                }
            }
        }
    }

    companion object {
        private const val RESOURCE_NAME = "ZMP_UTZ_MVM_04_V001"
        private const val NON_FAILURE_RET_CODE = "0"
    }
}

class SaveTaskStatus : ObjectRawStatus<SaveTaskResult>()