package com.lenta.bp14.models

import com.lenta.bp14.features.search_filter.FilterFieldType
import com.lenta.bp14.features.search_filter.FilterParameter
import com.lenta.bp14.models.general.ITaskType


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
        return true
    }

    override fun setTask(task: S?) {
        _task = task
    }

    override fun getCurrentTaskType(): ITaskType? {
        return _task?.getTaskType()
    }

}


interface ITaskManager<S : ITask, D : ITaskDescription> {

    fun getTask(): S?

    fun newTask(taskDescription: D): S?

    fun clearTask(): Boolean

    fun setTask(task: S?)

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

    fun getSupportedFiltersTypes(): Set<FilterFieldType> {
        return emptySet()
    }

    fun getFilterValue(filterFieldType: FilterFieldType): String? {
        return null
    }


    fun onFilterChanged(filterParameter: FilterParameter) {

    }

    fun addNewFilters(filters: List<FilterParameter>) {

    }

    fun clearAllFilters() {

    }

}

interface ITaskDescription {
    val tkNumber: String
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