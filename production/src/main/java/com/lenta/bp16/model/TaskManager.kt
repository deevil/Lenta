package com.lenta.bp16.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.pojo.Task
import com.lenta.bp16.platform.extention.getTaskType
import com.lenta.bp16.request.TaskInfoResult
import com.lenta.bp16.request.TaskListResult
import javax.inject.Inject

class TaskManager @Inject constructor() : ITaskManager {

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override lateinit var currentTask: Task

    override fun addTasks(taskListResult: TaskListResult) {
        val taskList = tasks.value!!.filter { it.isProcessed }.toMutableList()
        taskListResult.processingUnits.forEach { processingUnit ->
            val existTask = taskList.find { it.processingUnit.number == processingUnit.number }
            if (existTask == null) {
                taskList.add(Task(
                        type = processingUnit.getTaskType(),
                        processingUnit = processingUnit
                ))
            }
        }

        tasks.value = taskList
    }

    override fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult) {
        currentTask.apply {
            goods = taskInfoResult.goods
            raws = taskInfoResult.raws
            packs = taskInfoResult.packs
        }
    }

}

interface ITaskManager {
    val tasks: MutableLiveData<List<Task>>
    var currentTask: Task

    fun addTasks(taskListResult: TaskListResult)
    fun addTaskInfoToCurrentTask(taskInfoResult: TaskInfoResult)
}