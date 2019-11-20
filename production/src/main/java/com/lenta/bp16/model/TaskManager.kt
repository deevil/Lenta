package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.extention.getTaskType
import com.lenta.bp16.request.TaskListResult
import javax.inject.Inject

class TaskManager @Inject constructor() : ITaskManager {

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override fun addTasks(taskListResult: TaskListResult) {
        val taskList = tasks.value!!.filter { it.isProcessed }.toMutableList()
        taskListResult.processingUnits.forEach { processingUnit ->
            val existTask = taskList.find { it.processingUnit.number == processingUnit.number }
            if (existTask == null) {
                taskList.add(Task(
                        taskType = processingUnit.getTaskType(),
                        processingUnit = processingUnit
                ))
            }
        }

        tasks.value = taskList
    }

}

interface ITaskManager {
    val tasks: MutableLiveData<List<Task>>

    fun addTasks(taskListResult: TaskListResult)
}