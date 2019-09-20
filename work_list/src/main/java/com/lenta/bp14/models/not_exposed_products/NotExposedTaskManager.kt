package com.lenta.bp14.models.not_exposed_products

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskManager
import com.lenta.shared.platform.time.ITimeMonitor

class NotExposedTaskManager(private val timeMonitor: ITimeMonitor,
                            private val gson: Gson) : ITaskManager<INotExposedProductsTask, NotExposedProductsTaskDescription> {

    override var _task: INotExposedProductsTask? = null

    override fun newTask(taskDescription: NotExposedProductsTaskDescription): INotExposedProductsTask? {
        _task = NotExposedProductsTask(
                taskDescription = taskDescription

        )
        return _task
    }

}