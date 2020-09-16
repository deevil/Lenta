package com.lenta.bp9.features.base

import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.*

interface IBaseTaskManager {
    val taskManager: IReceivingTaskManager

    val receivingTask: ReceivingTask?
        get() = taskManager.getReceivingTask()

    val taskRepository: ITaskRepository?
        get() = taskManager.getReceivingTask()?.taskRepository

    val taskDescription: TaskDescription?
        get() = taskManager.getReceivingTask()?.taskDescription

    val taskHeader: TaskInfo?
        get() = taskManager.getReceivingTask()?.taskHeader

    val taskType: TaskType
        get() = taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.taskType
                ?: TaskType.None

    val taskNumber: String
        get() = taskHeader?.taskNumber.orEmpty()

    val processedProductsDiscrepancies: List<TaskProductDiscrepancies>?
        get() = receivingTask?.getProcessedProductsDiscrepancies()

    val processedBatchesDiscrepancies: List<TaskBatchesDiscrepancies>?
        get() = receivingTask?.getProcessedBatchesDiscrepancies()

    val processedBoxesDiscrepancies: List<TaskBoxDiscrepancies>?
        get() = receivingTask?.getProcessedBoxesDiscrepancies()

    val processedExciseStampsDiscrepancies: List<TaskExciseStampDiscrepancies>?
        get() = receivingTask?.getProcessedExciseStampsDiscrepancies()

    val processedExciseStampsBad: List<TaskExciseStampBad>?
        get() = receivingTask?.getProcessedExciseStampsBad()

    val processedMercuryDiscrepancies: List<TaskMercuryDiscrepancies>?
        get() = receivingTask?.getProcessedMercuryDiscrepancies()

    val processedBlocksDiscrepancies: List<TaskBlockDiscrepancies>?
        get() = receivingTask?.getProcessedBlocksDiscrepancies()

    val processedZBatchesDiscrepancies: List<TaskZBatchesDiscrepancies>?
        get() = receivingTask?.getProcessedZBatchesDiscrepancies()

}
