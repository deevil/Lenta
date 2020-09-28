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

fun IReceivingTaskManager.findManufacturerCodeInBatchOrEmpty(lastBatchNumber: String): String {
    return this.getReceivingTask()
            ?.getProcessedBatches()
            ?.findLast { it.batchNumber == lastBatchNumber }
            ?.egais
            .orEmpty()
}

fun IReceivingTaskManager.findBottlingDateInBatchOrDefault(lastBatchNumber: String, defaultDate: String?): String {
    return this.getReceivingTask()
            ?.getProcessedBatches()
            ?.findLast { it.batchNumber == lastBatchNumber }
            ?.bottlingDate
            ?: defaultDate.orEmpty()
}