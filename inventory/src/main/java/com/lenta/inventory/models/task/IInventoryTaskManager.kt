package com.lenta.inventory.models.task

interface IInventoryTaskManager {

    fun getInventoryTask() : InventoryTask?

    fun newInventoryTask(taskDescription: TaskDescription)

    fun clearTask()

    fun setTask(writeOffTask: InventoryTask?)

}