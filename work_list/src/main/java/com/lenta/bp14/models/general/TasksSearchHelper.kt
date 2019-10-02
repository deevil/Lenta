package com.lenta.bp14.models.general

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.requests.tasks.FilteredParams
import com.lenta.bp14.requests.tasks.SearchTaskFilter
import com.lenta.bp14.requests.tasks.SimpleParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.di.AppScope
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import javax.inject.Inject

@AppScope
class TasksSearchHelper @Inject constructor(
        private val sessionInfo: ISessionInfo,
        private val generalRepo: IGeneralRepo
) : ITasksSearchHelper {

    override var processedFilter: String? = ""

    override var searchFilter: String? = ""

    override var filterParams: SearchTaskFilter? = null

    override val taskList: MutableLiveData<List<TaskInfo>> = MutableLiveData(listOf())

    override val filteredTaskList: MutableLiveData<List<TaskInfo>> = MutableLiveData(listOf())

    override suspend fun updateTaskList(): Either<Failure, Boolean> {
        return generalRepo.getTaskList(SimpleParams(
                tkNumber = sessionInfo.market!!,
                user = processedFilter ?: ""
        )).map {
            taskList.postValue(it)
            true
        }
    }

    override suspend fun updateFilteredTaskList(): Either<Failure, Boolean> {
        return generalRepo.getFilteredTaskList(
                FilteredParams(
                        tkNumber = sessionInfo.market!!,
                        user = searchFilter ?: "",
                        filteredParams = filterParams
                )
        ).map {
            filteredTaskList.postValue(it)
            true
        }
    }


}

interface ITasksSearchHelper {
    var processedFilter: String?
    var searchFilter: String?
    var filterParams: SearchTaskFilter?

    val taskList: LiveData<List<TaskInfo>>
    val filteredTaskList: LiveData<List<TaskInfo>>

    suspend fun updateTaskList(): Either<Failure, Boolean>
    suspend fun updateFilteredTaskList(): Either<Failure, Boolean>


}