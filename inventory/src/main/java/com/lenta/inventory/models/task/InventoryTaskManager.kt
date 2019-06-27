package com.lenta.inventory.models.task

class InventoryTaskManager(private var currentWriteOffTask: InventoryTask? = null) : IInventoryTaskManager {
    override fun getInventoryTask(): InventoryTask? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newInventoryTask(taskDescription: TaskDescription) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearTask() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTask(inventoryTask: InventoryTask?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}