package com.lenta.bp9.model.task

import com.lenta.bp9.model.memory.MemoryTaskRepository

class ReceivingTaskManager : IReceivingTaskManager {

    private var currentReceivingTask: ReceivingTask? = null

    override fun getReceivingTask(): ReceivingTask? {
        return currentReceivingTask
    }

    override fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription) : ReceivingTask?
    {
        currentReceivingTask = ReceivingTask(taskHeader, taskDescription, taskRepository = MemoryTaskRepository())
        return  currentReceivingTask
    }

    override fun updateTaskDescription(taskDescription: TaskDescription) : ReceivingTask? {
        currentReceivingTask?.let {
            currentReceivingTask = ReceivingTask(it.taskHeader, taskDescription, it.taskRepository)
        }
        return currentReceivingTask
    }


    override fun clearTask() {
        currentReceivingTask = null
    }

    override fun setTask(receivingTask: ReceivingTask?) {
        currentReceivingTask = receivingTask
    }
}