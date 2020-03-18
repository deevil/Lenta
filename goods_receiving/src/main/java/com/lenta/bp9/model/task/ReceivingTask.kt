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

    fun getCargoUnits(): List<TaskCargoUnitInfo> {
        return taskRepository.getCargoUnits().getCargoUnits()
    }

    fun updateTaskWithContents(taskContentsInfo: TaskContentsInfo) {
        taskRepository.getProducts().updateProducts(taskContentsInfo.products)
        taskRepository.getProductsDiscrepancies().updateProductsDiscrepancy(taskContentsInfo.productsDiscrepancies)
        taskRepository.getBatches().updateBatches(taskContentsInfo.taskBatches)
        taskRepository.getBatchesDiscrepancies().updateBatchesDiscrepancy(taskContentsInfo.taskBatchesDiscrepancies)
        taskRepository.getMercuryDiscrepancies().updateMercuryInfo(taskContentsInfo.taskMercuryInfo)
        taskRepository.getExciseStamps().updateExciseStamps(taskContentsInfo.taskExciseStampInfo)
        taskRepository.getExciseStampDiscrepancies().updateExciseStampsDiscrepancy(taskContentsInfo.taskExciseStampDiscrepancies)
    }
}



