package com.lenta.bp9.model.task

interface IReceivingTaskManager {
    fun getReceivingTask() : ReceivingTask?

    fun newReceivingTask(taskDescription: TaskDescription) : ReceivingTask?

    fun clearTask()

    fun setTask(receivingTask: ReceivingTask?)
}