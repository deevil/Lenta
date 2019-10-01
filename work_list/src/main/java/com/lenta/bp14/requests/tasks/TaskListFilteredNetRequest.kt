package com.lenta.bp14.requests.tasks

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import javax.inject.Inject

class TaskListFilteredNetRequest
@Inject constructor(private val taskListNetRequest: TaskListNetRequest) : UseCase<TaskListInfo, FilteredParams> {
    override suspend fun run(params: FilteredParams): Either<Failure, TaskListInfo> {
        return taskListNetRequest(
                TasksListParams(
                        tkNumber = params.tkNumber,
                        user = params.user,
                        mode = "2",
                        filter = params.filteredParams
                )
        )
    }
}


data class FilteredParams(
        val tkNumber: String,
        val user: String,
        val filteredParams: SearchTaskFilter
)




