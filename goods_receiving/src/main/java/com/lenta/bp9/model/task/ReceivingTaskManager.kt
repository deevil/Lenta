package com.lenta.bp9.model.task

import com.lenta.bp9.model.memory.MemoryTaskRepository

class ReceivingTaskManager : IReceivingTaskManager {

    private var currentReceivingTask: ReceivingTask? = null

    override fun getReceivingTask(): ReceivingTask? {
        return currentReceivingTask
    }

    override fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription, notifications: List<TaskNotification>) : ReceivingTask?
    {
        currentReceivingTask = ReceivingTask(taskHeader, taskDescription, notifications, taskRepository = MemoryTaskRepository())
        return  currentReceivingTask
    }

    override fun clearTask() {
        currentReceivingTask = null
    }

    override fun setTask(receivingTask: ReceivingTask?) {
        currentReceivingTask = receivingTask
    }
}