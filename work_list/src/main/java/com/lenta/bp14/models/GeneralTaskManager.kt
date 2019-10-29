package com.lenta.bp14.models

import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.ITaskTypeInfo
import com.lenta.bp14.models.not_exposed_products.NotExposedProductsTaskManager
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.requests.pojo.SentReportResult
import com.lenta.shared.models.core.StateFromToString
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatDate
import com.lenta.shared.utilities.extentions.implementationOf
import javax.inject.Inject

class GeneralTaskManager @Inject constructor(
        checkPriceTaskManager: CheckPriceTaskManager,
        checkListTaskManager: CheckListTaskManager,
        workListTaskManager: WorkListTaskManager,
        notExposedProductsTaskManager: NotExposedProductsTaskManager,
        private val timeMonitor: ITimeMonitor,
        private val persistTaskData: IPersistTaskData
) : IGeneralTaskManager {

    private var latestSentReportResult: SentReportResult? = null

    private var savedTaskData: TaskData? = null

    private val allManagers = listOf<ITaskManager<*, *>>(checkPriceTaskManager, checkListTaskManager, workListTaskManager, notExposedProductsTaskManager)

    init {
        loadTaskData()
    }

    override fun getProcessedTaskType(): ITaskTypeInfo? {
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

    override fun generateNewNameForTask(taskTypeInfo: ITaskTypeInfo?): String {
        return if (taskTypeInfo == null || taskTypeInfo.taskType == AppTaskTypes.Empty.taskType) "" else {
            "${taskTypeInfo.taskName} от ${getCurrentTime()}"
        }
    }

    override fun getLatestSentReportResult(): SentReportResult? {
        return latestSentReportResult
    }

    private fun getCurrentTime(): String {
        return formatDate(timeMonitor.getUnixTime(), "${Constants.DATE_FORMAT_ddmm} ${Constants.TIME_FORMAT_HHmm}")
    }

    override fun isExistSavedTaskData(): Boolean {
        return savedTaskData != null
    }

    override fun saveTaskData() {
        getCurrentTaskManager()?.let { taskManager ->
            taskManager.getTask()?.implementationOf(StateFromToString::class.java)?.let { task ->
                persistTaskData.saveTaskData(TaskData(
                        taskType = taskManager.getTaskType(),
                        data = task.saveStateToString()
                ))
            }
        }
    }

    override fun loadTaskData() {
        persistTaskData.getSavedTaskData()?.let { taskData ->
            savedTaskData = taskData
        }
    }

    override fun getSavedData(): TaskData? {
        return savedTaskData
    }

    override fun clearSavedTaskData() {
        persistTaskData.clearSavedData()
    }

    override fun restoreSavedData() {
        getCurrentTaskManager()?.getTask()?.implementationOf(StateFromToString::class.java)?.let { task ->
            savedTaskData?.data?.let { data ->
                task.loadStateFromString(data)
            }
        }
    }

}


interface IGeneralTaskManager {
    fun getProcessedTask(): ITask?
    fun getProcessedTaskType(): ITaskTypeInfo?
    fun clearCurrentTask(sentReportResult: SentReportResult? = null): Boolean
    fun generateNewNameForTask(taskTypeInfo: ITaskTypeInfo?): String
    fun getLatestSentReportResult(): SentReportResult?
    fun isExistSavedTaskData(): Boolean
    fun saveTaskData()
    fun loadTaskData()
    fun getSavedData(): TaskData?
    fun clearSavedTaskData()
    fun restoreSavedData()
}