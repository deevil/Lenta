package com.lenta.bp14.models

import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.ITaskType

class GeneralTaskManager(private val checkPriceTaskManager: CheckPriceTaskManager) : IGeneralTaskManager {

    override fun getProcessedTaskType(): ITaskType? {
        return getCurrentTaskManager()?.getCurrentTaskType()
    }

    override fun clearCurrentTask(): Boolean {
        return checkPriceTaskManager.clearTask()
    }

    override fun getProcessedTask(): ITask? {
        return checkPriceTaskManager.getTask()
    }

    private fun getCurrentTaskManager(): ITaskManager<out ITask, out ITaskDescription>? {
        if (checkPriceTaskManager.getTask() != null) {
            return checkPriceTaskManager
        }
        return null
    }

}


interface IGeneralTaskManager {
    fun getProcessedTask(): ITask?
    fun getProcessedTaskType(): ITaskType?
    fun clearCurrentTask(): Boolean
}