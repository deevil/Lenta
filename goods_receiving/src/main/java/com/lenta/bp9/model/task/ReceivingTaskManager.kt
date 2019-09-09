package com.lenta.bp9.model.task

import com.lenta.bp9.model.memory.MemoryTaskRepository
import com.lenta.bp9.model.task.revise.*

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

    override fun newReceivingTaskFull(taskHeader: TaskInfo,
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
                                          complexDocumentsRevise: List<ComplexDocumentRevise>) : ReceivingTask? {
        currentReceivingTask = ReceivingTask(taskHeader,
                taskDescription,
                notifications,
                documentNotifications,
                productNotifications,
                conditionNotifications,
                deliveryDocumentsRevise,
                deliveryProductDocumentsRevise,
                productBatchesRevise,
                formsABRussianRevise,
                formsABImportRevise,
                setComponenttsRevise,
                invoiceRevise,
                commentsToVP,
                productsVetDocumentRevise,
                complexDocumentsRevise,
                taskRepository = MemoryTaskRepository())
        return currentReceivingTask
    }

    override fun clearTask() {
        currentReceivingTask = null
    }

    override fun setTask(receivingTask: ReceivingTask?) {
        currentReceivingTask = receivingTask
    }
}