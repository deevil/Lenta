package com.lenta.bp9.model.task

import com.lenta.bp9.model.task.revise.*

interface IReceivingTaskManager {
    fun getReceivingTask() : ReceivingTask?

    fun newReceivingTask(taskHeader: TaskInfo, taskDescription: TaskDescription, notifications: List<TaskNotification>) : ReceivingTask?

    fun newReceivingTaskFull(taskHeader: TaskInfo,
                             taskDescription: TaskDescription,
                             notifications: List<TaskNotification>,
                             documentNotifications: List<TaskNotification>,
                             productNotifications: List<TaskNotification>,
                             conditionNotifications: List<TaskNotification>,
                             deliveryDocumentsRevise: List<DeliveryDocumentRevise>,
                             deliveryProductDocumentsRevise: List<DeliveryProductDocumentRevise>,
                             productBatchesRevise: List<ProductBatchRevise>,
                             formsABRussianRevise: List<FormABRussianRevise>,
                             formsABImportRevise: List<FormABImportRevise>,
                             setComponenttsRevise: List<SetComponentRevise>,
                             invoiceRevise: InvoiceRevise?,
                             commentsToVP: List<CommentToVP>,
                             productsVetDocumentRevise: List<ProductVetDocumentRevise>,
                             complexDocumentsRevise: List<ComplexDocumentRevise>) : ReceivingTask?

    fun clearTask()

    fun setTask(receivingTask: ReceivingTask?)
}