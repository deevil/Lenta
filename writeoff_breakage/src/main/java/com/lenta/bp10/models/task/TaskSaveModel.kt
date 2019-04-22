package com.lenta.bp10.models.task

import com.lenta.bp10.models.repositories.ITaskRepository

class TaskSaveModel(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {
}