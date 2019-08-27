package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository

class ReceivingTask(val taskHeader: TaskInfo,
                    val taskDescription: TaskDescription,
                    val notifications: List<TaskNotification>,
                    val taskRepository: ITaskRepository) {
}