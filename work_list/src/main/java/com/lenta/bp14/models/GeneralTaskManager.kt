package com.lenta.bp14.models

import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.requests.pojo.SentReportResult
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatDate
import javax.inject.Inject

class GeneralTaskManager @Inject constructor(
        checkPriceTaskManager: CheckPriceTaskManager,
        checkListTaskManager: CheckListTaskManager,
        workListTaskManager: WorkListTaskManager,
        notExposedProductsTaskManager: NotExposedProductsTaskManager,
        private val timeMonitor: ITimeMonitor
) : IGeneralTaskManager {

    private var latestSentReportResult: SentReportResult? = null

    private val allManagers = listOf<ITaskManager<*, *>>(checkPriceTaskManager, checkListTaskManager, workListTaskManager, notExposedProductsTaskManager)

    override fun getProcessedTaskType(): ITaskType? {
        return getCurrentTaskManager()?.getCurrentTaskType()
    }

    override fun clearCurrentTask(sentReportResult: SentReportResult?): Boolean {
        this.latestSentReportResult = sentReportResult
        allManagers.forEach {
            if (it.clearTask()) {
                return true
            }
        }

        return false

    }

    override fun getProcessedTask(): ITask? {
        allManagers.forEach { manager ->
            manager.getTask()?.let {
                return it
            }
        }
        return null
    }

    private fun getCurrentTaskManager(): ITaskManager<out ITask, out ITaskDescription>? {
        allManagers.forEach { manager ->
            manager.getTask()?.let {
                return manager
            }
        }
        return null
    }

    override fun generateNewNameForTask(taskType: ITaskType?): String {
        return if (taskType == TaskTypes.Empty.taskType || taskType == null) "" else {
            "${taskType.taskName} от ${getCurrentTime()}"
        }
    }

    override fun getLatestSentReportResult(): SentReportResult? {
        return latestSentReportResult
    }

    private fun getCurrentTime(): String {
        return formatDate(timeMonitor.getUnixTime(), "${Constants.DATE_FORMAT_ddmm} ${Constants.TIME_FORMAT_HHmm}")
    }

}


interface IGeneralTaskManager {
    fun getProcessedTask(): ITask?
    fun getProcessedTaskType(): ITaskType?
    fun clearCurrentTask(sentReportResult: SentReportResult? = null): Boolean
    fun generateNewNameForTask(taskType: ITaskType?): String
    fun getLatestSentReportResult(): SentReportResult?
}