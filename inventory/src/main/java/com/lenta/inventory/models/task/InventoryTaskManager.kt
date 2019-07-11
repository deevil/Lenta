package com.lenta.inventory.models.task

import com.lenta.inventory.models.memory.MemoryTaskRepository

class InventoryTaskManager : IInventoryTaskManager {

    private var currentInventoryTask: InventoryTask? = null

    override fun getInventoryTask(): InventoryTask? {
        return currentInventoryTask
    }

    override fun newInventoryTask(taskDescription: TaskDescription) : InventoryTask? {
        currentInventoryTask = InventoryTask(taskDescription, taskRepository = MemoryTaskRepository())
        return  currentInventoryTask
    }

    override fun clearTask() {
        currentInventoryTask = null

    }

    override fun setTask(inventoryTask: InventoryTask?) {
        currentInventoryTask = inventoryTask
    }
}