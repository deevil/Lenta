package com.lenta.bp9.model.task

interface IReceivingTaskManager {
    fun getReceivingTask() : ReceivingTask?

    fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription, notifications: List<TaskNotification>) : ReceivingTask?

    fun clearTask()

    fun setTask(receivingTask: ReceivingTask?)
}