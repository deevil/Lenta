package com.lenta.bp14.models

import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.shared.di.CoreInjectHelper.removeComponent


abstract class BaseTaskManager<S : ITask, D : ITaskDescription> : ITaskManager<S, D> {
    protected var _task: S? = null

    override fun getTask(): S? {
        return _task
    }

    override fun clearTask(): Boolean {
        if (_task == null) {
            return false
        }
        _task = null
        removeComponent(getComponentClass())
        return true
    }

    abstract fun getComponentClass(): Class<out Any>

    override fun setTask(task: S?) {
        _task = task
    }

    override fun getCurrentTaskType(): ITaskTypeInfo? {
        return _task?.getTaskType()
    }

}


interface ITaskManager<S : ITask, D : ITaskDescription> {

    fun getTask(): S?

    fun newTask(taskDescription: D): S?

    fun clearTask(): Boolean

    fun setTask(task: S?)

    fun getCurrentTaskType(): ITaskTypeInfo?
}

interface ITask {
    fun getTaskType(): ITaskTypeInfo
    fun getDescription(): ITaskDescription
    /**
     * Проверяет режим работы. Возвращает:
     * true, если режим создания задания;
     * false - работы с заданием
     */
    fun isFreeMode(): Boolean {
        return this.getDescription().taskNumber.isBlank()
    }

    fun isEmpty(): Boolean

    fun isHaveDiscrepancies(): Boolean

}


interface ITaskDescription {
    val tkNumber: String
    val taskNumber: String
    var taskName: String
    val description: String
    val comment: String
    val isStrictList: Boolean
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

fun ITask.getTaskNumber(): String {
    return getDescription().taskNumber
}