package com.lenta.inventory.models.task

import com.lenta.inventory.models.memory.MemoryTaskRepository

class InventoryTaskManager : IInventoryTaskManager {

    private var currentWriteOffTask: InventoryTask? = null

    override fun getInventoryTask(): InventoryTask? {
        return currentWriteOffTask
    }

    override fun newInventoryTask(taskDescription: TaskDescription) {
        currentWriteOffTask = InventoryTask(taskDescription, taskRepository = MemoryTaskRepository())
    }

    override fun clearTask() {
        currentWriteOffTask = null

    }

    override fun setTask(writeOffTask: InventoryTask?) {
        currentWriteOffTask = writeOffTask
    }
}