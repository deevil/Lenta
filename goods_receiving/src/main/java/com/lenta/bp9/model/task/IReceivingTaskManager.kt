package com.lenta.bp9.model.task

import com.lenta.bp9.model.task.revise.*

interface IReceivingTaskManager {
    fun getReceivingTask() : ReceivingTask?

    fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription) : ReceivingTask?

    fun clearTask()

    fun setTask(receivingTask: ReceivingTask?)
}