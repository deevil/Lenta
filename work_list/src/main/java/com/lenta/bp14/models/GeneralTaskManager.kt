package com.lenta.bp14.models

import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatDate

class GeneralTaskManager(
        private val checkPriceTaskManager: CheckPriceTaskManager,
        private val checkListTaskManager: CheckListTaskManager,
        private val workListTaskManager: WorkListTaskManager,
        private var timeMonitor: ITimeMonitor
) : IGeneralTaskManager {
        private val notExposedProductsTaskManager: NotExposedProductsTaskManager,
        private var timeMonitor: ITimeMonitor) : IGeneralTaskManager {

    override fun getProcessedTaskType(): ITaskType? {
        return getCurrentTaskManager()?.getCurrentTaskType()
    }

    override fun clearCurrentTask(): Boolean {
        return checkPriceTaskManager.clearTask() || checkListTaskManager.clearTask() || notExposedProductsTaskManager.clearTask()
        return checkPriceTaskManager.clearTask() || checkListTaskManager.clearTask() || workListTaskManager.clearTask()
    }

    override fun getProcessedTask(): ITask? {
        return checkPriceTaskManager.getTask() ?: checkListTaskManager.getTask() ?: notExposedProductsTaskManager.getTask()
        return checkPriceTaskManager.getTask() ?: checkListTaskManager.getTask() ?: workListTaskManager.getTask()
    }

    private fun getCurrentTaskManager(): ITaskManager<out ITask, out ITaskDescription>? {
        if (checkPriceTaskManager.getTask() != null) {
            return checkPriceTaskManager
        }
        if (checkListTaskManager.getTask() != null) {
            return checkListTaskManager
        }
        if (notExposedProductsTaskManager.getTask() != null) {
            return notExposedProductsTaskManager
        }
        if (workListTaskManager.getTask() != null) {
            return workListTaskManager
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