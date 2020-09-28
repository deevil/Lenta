package com.lenta.bp9.model.task

interface IReceivingTaskManager {
    fun getReceivingTask() : ReceivingTask?

    fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription) : ReceivingTask?

    //creates new task with new description, but old contents (repositories)
    fun updateTaskDescription(taskDescription: TaskDescription): ReceivingTask?

    fun clearTask()

    fun setTask(receivingTask: ReceivingTask?)

    fun getTaskType(): TaskType
}

fun IReceivingTaskManager.getEgaisOrEmpty(lastBatchNumber: String): String {
    return this.getReceivingTask()
            ?.taskRepository
            ?.getBatches()
            ?.getBatches()
            ?.findLast { it.batchNumber == lastBatchNumber }
            ?.egais
            .orEmpty()
}

fun IReceivingTaskManager.getBottlingDateOrDefault(lastBatchNumber: String, defaultDate: String?): String {
    return this.getReceivingTask()
            ?.taskRepository
            ?.getBatches()
            ?.getBatches()
            ?.findLast { it.batchNumber == lastBatchNumber }
            ?.bottlingDate
            ?: defaultDate.orEmpty()
}