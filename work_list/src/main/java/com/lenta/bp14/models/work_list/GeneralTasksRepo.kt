package com.lenta.bp14.models.work_list

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.requests.tasks.FilteredParams
import com.lenta.bp14.requests.tasks.SimpleParams
import com.lenta.shared.interactor.UseCase


interface IGeneralTasksRepo {
    val taskList: LiveData<List<TaskInfo>>
    val filteredTaskList: LiveData<List<TaskInfo>>
    suspend fun updateTasks(params: SimpleParams): UseCase<Boolean, SimpleParams>
    suspend fun updateFilteredTasks(params: FilteredParams): UseCase<Boolean, FilteredParams>
}


data class TaskInfo(
        val taskId: String,
        val taskType: ITaskType,
        val taskName: String,
        val isNotFinished: String,
        val isMyBlock: Boolean,
        val isStrict: Boolean
)

/*class GeneralTasksRepo(taskListNetRequest: UseCase<TaskListInfo, TasksListParams>) : IGeneralTasksRepo {

    override val taskList: LiveData<List<TaskInfo>> = MutableLiveData(emptyList())

    override val filteredTaskList: LiveData<List<TaskInfo>> = MutableLiveData(emptyList())


}*/
