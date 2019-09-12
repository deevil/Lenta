package com.lenta.bp14.models

import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatDate

class GeneralTaskManager(
        private val checkPriceTaskManager: CheckPriceTaskManager,
        private var timeMonitor: ITimeMonitor) : IGeneralTaskManager {

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

    override fun generateNewNameForTask(taskType: ITaskType?): String {
        return if (taskType == TaskTypes.Empty.taskType || taskType == null) "" else {
            "${taskType.taskName} от ${getCurrentTime()}"
        }
    }

    private fun getCurrentTime(): String {
        return formatDate(timeMonitor.getUnixTime(), "${Constants.DATE_FORMAT_ddmm} ${Constants.TIME_FORMAT_HHmm}")
    }

}


interface IGeneralTaskManager {
    fun getProcessedTask(): ITask?
    fun getProcessedTaskType(): ITaskType?
    fun clearCurrentTask(): Boolean
    fun generateNewNameForTask(taskType: ITaskType?): String
}