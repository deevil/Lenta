package com.lenta.bp14.models

import com.lenta.bp14.models.general.ITaskType


interface ITaskManager<S : ITask, D : ITaskDescription> {

    var _task: S?

    fun getTask(): S? {
        return _task
    }

    fun newTask(taskDescription: D): S?

    fun clearTask(): Boolean {
        if (_task == null) {
            return false
        }
        _task = null
        return true
    }

    fun setTask(task: S?) {
        _task = task
    }

    fun getCurrentTaskType(): ITaskType? {
        return _task?.getTaskType()
    }
}

interface ITask {
    fun getTaskType(): ITaskType
    fun getDescription(): ITaskDescription
    /**
     * Проверяет режим работы. Возвращает:
     * true, если режим создания задания;
     * false - работы с заданием
     */
    fun isFreeMode(): Boolean {
        return this.getDescription().taskNumber.isBlank()
    }
}

interface ITaskDescription {
    val taskNumber: String
    var taskName: String
    val description: String
    val comment: String
}

fun ITaskManager<*, *>.getTaskName(): String? {
    return this.getTask()?.getDescription()?.taskName ?: ""
}

fun ITaskManager<*, *>.getTaskType(): String? {
    return this.getTask()?.getTaskType()?.taskType ?: ""
}

fun ITask.getTaskName(): String {
    return getDescription().taskName
}