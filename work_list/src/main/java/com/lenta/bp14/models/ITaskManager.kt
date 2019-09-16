package com.lenta.bp14.models

import com.lenta.bp14.models.general.ITaskType


interface ITaskManager<S : ITask, D : ITaskDescription> {

    fun getTask(): S?

    fun newTask(taskDescription: D): S?

    fun clearTask(): Boolean

    fun setTask(inventoryTask: S?)

    fun getCurrentTaskType(): ITaskType?
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