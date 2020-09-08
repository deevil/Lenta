package com.lenta.bp9.features.base

import com.lenta.bp9.model.repositories.ITaskProductsDiscrepanciesRepository
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.*

interface IBaseTaskManager {
    val taskManager: IReceivingTaskManager

    val receivingTask: ReceivingTask?
        get() = taskManager.getReceivingTask()

    val taskRepository : ITaskRepository?
        get() = taskManager.getReceivingTask()?.taskRepository

    val taskDescription : TaskDescription?
        get() = taskManager.getReceivingTask()?.taskDescription

    val taskHeader : TaskInfo?
        get() = taskManager.getReceivingTask()?.taskHeader

    val taskType : TaskType
        get() = taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.taskType
                ?: TaskType.None

    val productsDiscrepancies : ITaskProductsDiscrepanciesRepository?
        get() = taskRepository?.getProductsDiscrepancies()
}
