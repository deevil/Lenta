package com.lenta.bp14.models.general


class GeneralRepo : IGeneralRepo {

    override suspend fun getTasksTypes(): List<ITaskType> {
        return listOf(
                TaskTypes.Empty.taskType,
                TaskTypes.CheckList.taskType,
                TaskTypes.CheckPrice.taskType,
                TaskTypes.NotExposedProducts.taskType
        )
    }

}

interface IGeneralRepo {
    suspend fun getTasksTypes(): List<ITaskType>
}


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
    WorkList(
            taskType = TaskType(
                    taskType = "РБС",
                    taskName = "Рабочий список",
                    annotation = "Задание для создания рабочего списка"
            )
    ),
    NotExposedProducts(
            taskType = TaskType(
                    taskType = "НВТ",
                    taskName = "Невыставленный товар",
                    annotation = "Невыставленный товар"

            )
    )

}

interface ITaskType {
    val taskType: String
    val taskName: String
    val annotation: String
}