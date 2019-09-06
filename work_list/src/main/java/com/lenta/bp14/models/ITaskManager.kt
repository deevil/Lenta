package com.lenta.bp14.models


interface ITaskManager<S : ITask, D : ITaskDescription> {

    fun getTask(): S?

    fun newTask(taskDescription: D?): S?

    fun clearTask()

    fun setTask(inventoryTask: S?)

}

interface ITask
interface ITaskDescription