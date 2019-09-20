package com.lenta.bp14.models.not_exposed_products

import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes


class NotExposedProductsTask(private val taskDescription: NotExposedProductsTaskDescription) : INotExposedProductsTask {
    override fun getTaskType(): ITaskType {
        return TaskTypes.NotExposedProducts.taskType
    }

    override fun getDescription(): ITaskDescription {
        return taskDescription
    }

}


interface INotExposedProductsTask : ITask {

}