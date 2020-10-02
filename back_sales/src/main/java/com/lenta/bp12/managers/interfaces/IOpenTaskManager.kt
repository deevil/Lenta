package com.lenta.bp12.managers.interfaces

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.open_task.TaskOpen
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.bp12.request.pojo.TaskSearchParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult

/**
 * implementation:
 * @See com.lenta.bp12.managers.OpenTaskManager
 * */
interface IOpenTaskManager : ITaskManager<TaskOpen> {

    var isNeedLoadTaskListByParams: Boolean
    var searchParams: TaskSearchParams?

    val tasks: MutableLiveData<List<TaskOpen>>
    val foundTasks: MutableLiveData<List<TaskOpen>>

    fun deleteGoodsFromBaskets(materials: List<String>)

    fun updateTasks(taskList: List<TaskOpen>?)
    fun updateFoundTasks(taskList: List<TaskOpen>?)

    fun isGoodCorrespondToTask(goodInfo: GoodInfoResult): Boolean
    fun finishCurrentTask()
    suspend fun addTasks(tasksInfo: List<TaskInfo>)
    suspend fun addFoundTasks(tasksInfo: List<TaskInfo>)
    suspend fun addTaskContentInCurrentTask(taskContentResult: TaskContentResult)
    fun markGoodsDeleted(materials: List<String>)
    fun markGoodsUncounted(materials: List<String>)
    fun isExistStartTaskInfo(): Boolean
    fun saveStartTaskInfo()
    fun isTaskWasChanged(): Boolean
    fun clearStartTaskInfo()
    fun clearCurrentTask()
}