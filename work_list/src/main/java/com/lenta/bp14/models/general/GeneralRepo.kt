package com.lenta.bp14.models.general

import com.lenta.bp14.fmp.resources.ZfmpUtz49V001
import com.lenta.bp14.requests.tasks.FilteredParams
import com.lenta.bp14.requests.tasks.ITaskListFilteredNetRequest
import com.lenta.bp14.requests.tasks.SimpleParams
import com.lenta.bp14.requests.tasks.TaskListUpdateNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getAllowedWklAppVersion
import com.lenta.shared.fmp.resources.dao_ext.getMaxPositionsProdWkl
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class GeneralRepo @Inject constructor(
        private val taskListUpdateNetRequest: TaskListUpdateNetRequest,
        private val taskListFilteredNetRequest: ITaskListFilteredNetRequest,
        private val hyperHive: HyperHive
) : IGeneralRepo {

    private val taskTypes by lazy { ZfmpUtz49V001(hyperHive) } // Типы заданий
    private val settings: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) } // Настройки

    private val emptyTaskTypeInfo = TaskTypeInfo(
            taskType = "",
            taskName = "Не выбрано",
            annotation = "Не выбрано"
    )

    val taskTypesInfoList: MutableList<ITaskTypeInfo> = mutableListOf()

    override suspend fun getTasksTypes(): List<ITaskTypeInfo> {
        return withContext(Dispatchers.IO) {
            @Suppress("INACCESSIBLE_TYPE")
            val serverTaskTypeInfoList = taskTypes.localHelper_ET_TASK_TPS.all
            val serverTaskTypes = serverTaskTypeInfoList.map { it.taskType to it }.toMap()

            return@withContext AppTaskTypes.values().filter { it === AppTaskTypes.Empty || serverTaskTypes.contains(it.taskType) }.map {
                if (it == AppTaskTypes.Empty) {
                    emptyTaskTypeInfo
                } else {
                    serverTaskTypes[it.taskType].let { serverInfo ->
                        requireNotNull(serverInfo)
                        TaskTypeInfo(
                                taskType = serverInfo.taskType,
                                taskName = serverInfo.taskName,
                                annotation = serverInfo.annotation
                        )
                    }
                }
            }.apply {
                taskTypesInfoList.clear()
                taskTypesInfoList.addAll(this)
            }
        }
    }

    private val funcAdapter = { taskInfo: com.lenta.bp14.requests.tasks.TaskInfo ->
        TaskInfo(
                taskId = taskInfo.taskNumber,
                taskTypeInfo = getTasksTypeInfo(taskInfo.taskType) ?: emptyTaskTypeInfo,
                taskNumber = taskInfo.text1,
                taskName = taskInfo.taskName,
                isNotFinished = taskInfo.notFinished.isSapTrue(),
                isMyBlock = if (taskInfo.blockType.isBlank()) null else taskInfo.blockType == "1",
                blockingUser = taskInfo.lockUser,
                isStrict = taskInfo.isStrict.isSapTrue(),
                quantityPositions = taskInfo.quantityPositions,
                comment = taskInfo.comment
        )
    }

    override suspend fun getFilteredTaskList(filterParams: FilteredParams): Either<Failure, List<TaskInfo>> {
        return taskListFilteredNetRequest(filterParams).map {
            it.taskList.map(funcAdapter)
        }
    }

    override suspend fun getTaskList(params: SimpleParams): Either<Failure, List<TaskInfo>> {
        return taskListUpdateNetRequest(params).map {
            it.taskList.map(funcAdapter)
        }
    }

    override fun getTasksTypeInfo(taskType: String): ITaskTypeInfo? {
        return taskTypesInfoList.firstOrNull { it.taskType == taskType }
    }

    override suspend fun onDbReady() {
        getTasksTypes()
    }

    override suspend fun getAllowedAppVersion(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getAllowedWklAppVersion()
        }
    }

    override suspend fun getMaxTaskPositions(): Double? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getMaxPositionsProdWkl()
        }
    }

}


interface IGeneralRepo {
    suspend fun getTasksTypes(): List<ITaskTypeInfo>
    suspend fun getTaskList(params: SimpleParams): Either<Failure, List<TaskInfo>>
    suspend fun getFilteredTaskList(filterParams: FilteredParams): Either<Failure, List<TaskInfo>>
    fun getTasksTypeInfo(taskType: String): ITaskTypeInfo?
    suspend fun onDbReady()
    suspend fun getAllowedAppVersion(): String?
    suspend fun getMaxTaskPositions(): Double?
}

data class TaskInfo(
        val taskId: String,
        val taskTypeInfo: ITaskTypeInfo,
        val taskNumber: String,
        val taskName: String,
        val comment: String,
        val isNotFinished: Boolean,
        val isMyBlock: Boolean?,
        val isStrict: Boolean,
        val blockingUser: String,
        val quantityPositions: Int
)

data class TaskTypeInfo(
        override val taskType: String,
        override val taskName: String,
        override val annotation: String
) : ITaskTypeInfo

enum class AppTaskTypes(val taskType: String) {
    Empty(taskType = ""),
    CheckList(taskType = "ЧКЛ"),
    CheckPrice(taskType = "СЦН"),
    NotExposedProducts(taskType = "НТП"),
    WorkList(taskType = "РБС")
}

interface ITaskTypeInfo {
    val taskType: String
    val taskName: String
    val annotation: String
}