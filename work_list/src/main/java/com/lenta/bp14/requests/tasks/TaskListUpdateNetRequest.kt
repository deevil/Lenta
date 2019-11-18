package com.lenta.bp14.requests.tasks

import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

@AppScope
class TaskListUpdateNetRequest
@Inject constructor(private val taskListNetRequest: TaskListNetRequest) : ITaskListUpdateNetRequest {

    override suspend fun run(params: SimpleParams): Either<Failure, TaskListInfo> {
        return taskListNetRequest(
                TasksListParams(
                        tkNumber = params.tkNumber,
                        user = params.user,
                        mode = "1",
                        filter = null
                )
        )
    }

}

interface ITaskListUpdateNetRequest : UseCase<TaskListInfo, SimpleParams>

data class SimpleParams(
        val tkNumber: String,
        val user: String
)



