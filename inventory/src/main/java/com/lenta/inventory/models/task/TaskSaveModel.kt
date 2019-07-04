package com.lenta.inventory.models.task

import javax.inject.Inject

class TaskSaveModel(val taskDescription: TaskDescription) {
    @Inject
    lateinit var inventoryTask: InventoryTask
}