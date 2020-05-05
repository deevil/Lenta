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

    fun getObligatoryDeliveryDocuments(): List<DeliveryDocumentRevise> {
        return taskRepository.getReviseDocuments().getDeliveryDocuments().filter { it.isObligatory }
    }

    fun updateTaskWithContents(taskContentsInfo: TaskContentsInfo) {
        taskRepository.getProducts().updateProducts(taskContentsInfo.products)
        taskRepository.getProductsDiscrepancies().updateProductsDiscrepancy(taskContentsInfo.productsDiscrepancies)
        taskRepository.getBatches().updateBatches(taskContentsInfo.taskBatches)
        taskRepository.getBatchesDiscrepancies().updateBatchesDiscrepancy(taskContentsInfo.taskBatchesDiscrepancies)
        taskContentsInfo.taskMercuryInfo?.let { taskRepository.getMercuryDiscrepancies().updateMercuryInfo(it) }
        taskRepository.getExciseStamps().updateExciseStamps(taskContentsInfo.taskExciseStampInfo)
        taskRepository.getExciseStampsDiscrepancies().updateExciseStampsDiscrepancy(taskContentsInfo.taskExciseStampDiscrepancies)
        taskRepository.getExciseStampsBad().updateExciseStampBad(taskContentsInfo.taskExciseStampBad)
        taskRepository.getBoxes().updateBoxes(taskContentsInfo.taskBoxes)
        taskRepository.getBoxesDiscrepancies().updateBoxesDiscrepancy(taskContentsInfo.taskBoxesDiscrepancies)
    }

    //количество коробов для товара прошедших контроль
    fun countBoxesPassedControlOfProduct(productInfo: TaskProductInfo) : Int { //https://trello.com/c/Z1SPfmAJ-контроль коробов, https://trello.com/c/Hve509E5 - контроль короба
        return taskRepository.getBoxes().findBoxesOfProduct(productInfo)?.filter {taskBoxInfo ->
            val countProcessedBoxes = taskRepository.getBoxesDiscrepancies().findBoxesDiscrepanciesOfProduct(productInfo).filter {
                it.boxNumber == taskBoxInfo.boxNumber
            }.size
            val countScannedExciseStamps = taskRepository.getExciseStampsDiscrepancies().findExciseStampsDiscrepanciesOfProduct(productInfo).filter {
                it.isScan && it.boxNumber == taskBoxInfo.boxNumber && it.typeDiscrepancies == "1"
            }.size
            (countProcessedBoxes >= 1 && countScannedExciseStamps >= 1) || (countScannedExciseStamps >= 2) || (countScannedExciseStamps >= productInfo.numberStampsControl.toInt())
        }?.size ?: 0
    }

    //контроль коробов для товара пройден
    fun controlBoxesOfProduct(productInfo: TaskProductInfo) : Boolean { //https://trello.com/c/Z1SPfmAJ
        return countBoxesPassedControlOfProduct(productInfo) >= productInfo.numberBoxesControl.toInt()
    }

    //контроль марок в коробах для товара пройден
    fun controlExciseStampsOfProduct(productInfo: TaskProductInfo) : Boolean { //https://trello.com/c/Z1SPfmAJ
        val countScannedExciseStampsDiscrepanciesOfProduct = taskRepository.getExciseStampsDiscrepancies().findExciseStampsDiscrepanciesOfProduct(productInfo).filter {
            it.isScan && it.materialNumber == productInfo.materialNumber && it.typeDiscrepancies == "1"
        }.size

        return controlBoxesOfProduct(productInfo) && countScannedExciseStampsDiscrepanciesOfProduct >= productInfo.numberStampsControl.toInt()
    }
}



