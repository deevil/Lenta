package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class TaskSaveModel(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {
}