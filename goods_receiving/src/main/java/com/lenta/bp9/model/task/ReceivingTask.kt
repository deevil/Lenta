package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository

class ReceivingTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {
}