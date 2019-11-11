package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.revise.*
import com.mobrun.plugin.api.HyperHive

class ReceivingTask(val taskHeader: TaskInfo,
                    val taskDescription: TaskDescription,
                    val taskRepository: ITaskRepository) {

    fun getUncheckedDeliveryDocuments(): List<DeliveryDocumentRevise> {
        return taskRepository.getReviseDocuments().getDeliveryDocuments().filter { !it.isCheck }
    }

    fun getCheckedDeliveryDocuments(): List<DeliveryDocumentRevise> {
        return taskRepository.getReviseDocuments().getDeliveryDocuments().filter { it.isCheck }
    }

    fun getUncheckedProductDocuments(): List<DeliveryProductDocumentRevise> {
        return taskRepository.getReviseDocuments().getProductDocuments().filter { !it.isCheck }
    }

    fun getCheckedProductDocuments(): List<DeliveryProductDocumentRevise> {
        return taskRepository.getReviseDocuments().getProductDocuments().filter { it.isCheck }
    }

    fun getProcessedProducts(): List<TaskProductInfo> {
        return taskRepository.getProducts().getProducts()
    }

    fun getProcessedBatches(): List<TaskBatchInfo> {
        return taskRepository.getBatches().getBatches()
    }

    fun getProcessedSections(): List<TaskSectionInfo> {
        return taskRepository.getSections().getSections()
    }

    private fun updateProducts(products: List<TaskProductInfo>) {
        taskRepository.getProducts().clear()
        taskRepository.getProducts().updateProducts(products)
    }

    private fun updateProductsDiscrepancies(productsDiscrepancies: List<TaskProductDiscrepancies>) {
        taskRepository.getProductsDiscrepancies().clear()
        taskRepository.getProductsDiscrepancies().updateProductsDiscrepancy(productsDiscrepancies)
    }

    private fun updateBatches(batches: List<TaskBatchInfo>) {
        taskRepository.getBatches().clear()
        taskRepository.getBatches().updateBatches(batches)
    }

    private fun updateBatchesDiscrepancies(batchesDiscrepancies: List<TaskBatchesDiscrepancies>) {
        taskRepository.getBatchesDiscrepancies().clear()
        taskRepository.getBatchesDiscrepancies().updateBatchesDiscrepancy(batchesDiscrepancies)
    }

    fun updateTaskWithContents(taskContentsInfo: TaskContentsInfo) {
        updateProducts(taskContentsInfo.products)
        updateProductsDiscrepancies(taskContentsInfo.productsDiscrepancies)
        updateBatches(taskContentsInfo.taskBatches)
        updateBatchesDiscrepancies(taskContentsInfo.taskBatchesDiscrepancies)
    }
}



