package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.revise.*

class ReceivingTask(val taskHeader: TaskInfo,
                    val taskDescription: TaskDescription,
                    val notifications: List<TaskNotification>,
                    val documentNotifications: List<TaskNotification> = emptyList(),
                    val productNotifications: List<TaskNotification> = emptyList(),
                    val conditionNotifications: List<TaskNotification> = emptyList(),
                    val deliveryDocumentsRevise: List<DeliveryDocumentRevise> = emptyList(),
                    val deliveryProductDocumentsRevise: List<DeliveryProductDocumentRevise> = emptyList(),
                    val productBatchesRevise: List<ProductBatchRevise> = emptyList(),
                    val formsABRussianRevise: List<FormABRussianRevise> = emptyList(),
                    val formsABImportRevise: List<FormABImportRevise> = emptyList(),
                    val setComponenttsRevise: List<SetComponentRevise> = emptyList(),
                    val invoiceRevise: InvoiceRevise? = null,
                    val commentsToVP: List<CommentToVP> = emptyList(),
                    val productsVetDocumentRevise: List<ProductVetDocumentRevise> = emptyList(),
                    val complexDocumentsRevise: List<ComplexDocumentRevise> = emptyList(),
                    val taskRepository: ITaskRepository) {
}



