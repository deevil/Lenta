package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository

class ReceivingTaskManager
constructor(
        private val memoryTaskRepository: ITaskRepository
) : IReceivingTaskManager {

    private var currentReceivingTask: ReceivingTask? = null

    override fun getReceivingTask(): ReceivingTask? {
        return currentReceivingTask
    }

    override fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription) : ReceivingTask?
    {
        currentReceivingTask = ReceivingTask(taskHeader, taskDescription, taskRepository = memoryTaskRepository)
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

    override fun getTaskType(): TaskType {
        return currentReceivingTask?.taskHeader?.taskType ?: TaskType.None
    }
}