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
}

interface ITaskDescription {
    var taskName: String
    var description: String
    var comment: String
}