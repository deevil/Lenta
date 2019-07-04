package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class ProcessExciseAlcoProductService(val taskDescription: TaskDescription,
                                      val taskRepository: ITaskRepository,
                                      val productInfo: TaskProductInfo) : IProcessProductService {
    override fun getTotalCount(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun apply(): InventoryTask {
        return InventoryTask(taskDescription, taskRepository)
    }

    override fun discard(): InventoryTask {
        return InventoryTask(taskDescription, taskRepository)
    }

    fun add(){
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}