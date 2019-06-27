package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository
import org.joda.time.DateTime

class InventoryTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository, val taskDeadLine : DateTime) {

    fun UpdateStorplaces() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun ClearStorplace() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun DeleteStorplace() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getTaskSaveModel(): TaskSaveModel {
        return TaskSaveModel(taskDescription, taskRepository)
    }

    fun clearTask() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}