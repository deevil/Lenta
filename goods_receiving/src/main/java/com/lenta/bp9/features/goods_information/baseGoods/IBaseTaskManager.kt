package com.lenta.bp9.features.goods_information.baseGoods

import com.lenta.bp9.model.repositories.ITaskProductsDiscrepanciesRepository
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.TaskType

interface IBaseTaskManager : IBaseVariables {
    val taskRepository : ITaskRepository?
        get() = taskManager.getReceivingTask()?.taskRepository

    val taskType : TaskType
        get() = taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.taskType
                ?: TaskType.None

    val productsDiscrepancies : ITaskProductsDiscrepanciesRepository?
        get() = taskRepository?.getProductsDiscrepancies()
}
