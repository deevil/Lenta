package com.lenta.bp14.models.general

import com.lenta.bp14.requests.tasks.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.map
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject


class GeneralRepo @Inject constructor(
        private val taskListUpdateNetRequest: TaskListUpdateNetRequest,
        private val taskListFilteredNetRequest: ITaskListFilteredNetRequest
) : IGeneralRepo {


    override suspend fun getTasksTypes(): List<ITaskType> {
        return listOf(
                TaskTypes.Empty.taskType,
                TaskTypes.CheckList.taskType,
                TaskTypes.CheckPrice.taskType,
                TaskTypes.NotExposedProducts.taskType,
                TaskTypes.WorkList.taskType
        )
    }

    private val funcAdapter = { taskInfo: com.lenta.bp14.requests.tasks.TaskInfo ->
        TaskInfo(
                taskId = taskInfo.taskNumber,
                taskType = taskInfo.taskType.toTaskType(),
                taskName = taskInfo.taskName,
                isNotFinished = taskInfo.notFinished.isSapTrue(),
                isMyBlock = taskInfo.blockType == "1",
                isStrict = taskInfo.isStrict.isSapTrue(),
                quantityPositions = taskInfo.quantityPositions
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
}

private fun String.toTaskType(): ITaskType {
    return TaskTypes.values().find { it.taskType.taskType == this }?.let { it.taskType }
            ?: TaskTypes.Empty.taskType
}

interface IGeneralRepo {
    suspend fun getTasksTypes(): List<ITaskType>
    suspend fun getTaskList(params: SimpleParams): Either<Failure, List<TaskInfo>>
    suspend fun getFilteredTaskList(filterParams: FilteredParams): Either<Failure, List<TaskInfo>>
}

data class TaskInfo(
        val taskId: String,
        val taskType: ITaskType,
        val taskName: String,
        val isNotFinished: Boolean,
        val isMyBlock: Boolean,
        val isStrict: Boolean,
        val quantityPositions: Int
)


data class TaskType(
        override val taskType: String,
        override val taskName: String,
        override val annotation: String

) : ITaskType

enum class TaskTypes(val taskType: ITaskType) {
    Empty(
            taskType = TaskType(
                    taskType = "",
                    taskName = "Не выбрано",
                    annotation = "Не выбрано"

            )),
    CheckList(
            taskType = TaskType(
                    taskType = "ЧКЛ",
                    taskName = "Чек лист",
                    annotation = "Чек лист"

            )),
    CheckPrice(
            taskType = TaskType(
                    taskType = "СЦН",
                    taskName = "Сверка цен",
                    annotation = "Сверка цен"

            )
    ),
    NotExposedProducts(
            taskType = TaskType(
                    taskType = "НТП",
                    taskName = "Невыставленный товар",
                    annotation = "Невыставленный товар"

            )
    ),
    WorkList(
            taskType = TaskType(
                    taskType = "РБС",
                    taskName = "Рабочий список",
                    annotation = "Задание для создания рабочего списка"
            )
    )

}

interface ITaskType {
    val taskType: String
    val taskName: String
    val annotation: String
}