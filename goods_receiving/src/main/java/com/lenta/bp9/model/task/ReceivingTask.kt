package com.lenta.bp9.model.task

import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.models.core.ProductType

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

    fun getProcessedProductsDiscrepancies(): List<TaskProductDiscrepancies> {
        return taskRepository.getProductsDiscrepancies().getProductsDiscrepancies()
    }

    fun getProcessedBatches(): List<TaskBatchInfo> {
        return taskRepository.getBatches().getBatches()
    }

    fun getProcessedBatchesDiscrepancies(): List<TaskBatchesDiscrepancies> {
        return taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies()
    }

    fun getProcessedBoxes(): List<TaskBoxInfo> {
        return taskRepository.getBoxes().getBoxes()
    }

    fun getProcessedBoxesDiscrepancies(): List<TaskBoxDiscrepancies> {
        return taskRepository.getBoxesDiscrepancies().getBoxesDiscrepancies()
    }

    fun getProcessedExciseStampsBad(): List<TaskExciseStampBad> {
        return taskRepository.getExciseStampsBad().getExciseStampsBad()
    }

    fun getProcessedMercuryDiscrepancies(): List<TaskMercuryDiscrepancies> {
        return taskRepository.getMercuryDiscrepancies().getMercuryDiscrepancies()
    }

    fun getProcessedBlocksDiscrepancies(): List<TaskBlockDiscrepancies> {
        return taskRepository.getBlocksDiscrepancies().getBlocksDiscrepancies()
    }

    fun getProcessedExciseStampsDiscrepancies(): List<TaskExciseStampDiscrepancies> {
        return taskRepository.getExciseStampsDiscrepancies().getExciseStampDiscrepancies()
    }

    fun getProcessedBlocks(): List<TaskBlockInfo> {
        return taskRepository.getBlocks().getBlocks()
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
        taskRepository
                .getProducts()
                .updateProducts(taskContentsInfo.products)
        taskRepository
                .getProductsDiscrepancies()
                .updateProductsDiscrepancy(taskContentsInfo.productsDiscrepancies)
        taskRepository
                .getBatches()
                .updateBatches(taskContentsInfo.taskBatches)
        taskRepository
                .getBatchesDiscrepancies()
                .updateBatchesDiscrepancy(taskContentsInfo.taskBatchesDiscrepancies)

        /**т.к. для не акцизного алкоголя (партионных товаров) инфа приходит в TaskBatchInfo и TaskBatchesDiscrepancies, а в TaskProductInfo и TaskProductDiscrepancies по этим товарам пусто,
        а на экранах Списсок товаров и Обнаружены расхождения отображается инфа из TaskProductInfo и TaskProductDiscrepancies, то для корректного отображения,
        необходимо добавить инфу из TaskBatchesDiscrepancies в TaskProductDiscrepancies, перед сохранением (вызов 12 реста) инфа по партионным товарам все равно удаляется из TaskProductDiscrepancies
         */
        taskContentsInfo.products.map { productInfo ->
            if (productInfo.type == ProductType.NonExciseAlcohol
                    && !productInfo.isBoxFl
                    && !productInfo.isMarkFl) {
                taskRepository
                        .getBatchesDiscrepancies()
                        .findBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                        .map {
                            changeProductDiscrepancyToBatch(
                                    productInfo = productInfo,
                                    batchDiscrepancies = it
                            )
                        }
            }
        }

        taskContentsInfo
                .taskMercuryInfo
                ?.let {
                    taskRepository
                            .getMercuryDiscrepancies()
                            .updateMercuryInfo(it)
                }
        taskRepository
                .getExciseStamps()
                .updateExciseStamps(taskContentsInfo.taskExciseStampInfo)
        taskRepository
                .getExciseStampsDiscrepancies()
                .updateExciseStampsDiscrepancy(taskContentsInfo.taskExciseStampDiscrepancies)
        taskRepository
                .getExciseStampsBad()
                .updateExciseStampBad(taskContentsInfo.taskExciseStampBad)
        taskRepository
                .getBoxes()
                .updateBoxes(taskContentsInfo.taskBoxes)
        taskRepository
                .getBoxesDiscrepancies()
                .updateBoxesDiscrepancies(taskContentsInfo.taskBoxesDiscrepancies)
        taskRepository
                .getBlocks()
                .updateBlocks(taskContentsInfo.taskBlock)
        taskRepository
                .getBlocksDiscrepancies()
                .updateBlocksDiscrepancies(taskContentsInfo.taskBlockDiscrepancies)
    }

    //количество коробов для товара прошедших контроль
    fun countBoxesPassedControlOfProduct(productInfo: TaskProductInfo): Int { //https://trello.com/c/Z1SPfmAJ-контроль коробов, https://trello.com/c/Hve509E5 - контроль короба
        return taskRepository
                .getBoxes()
                .findBoxesOfProduct(productInfo)
                ?.filter { taskBoxInfo ->
                    val countProcessedBoxes = taskRepository
                            .getBoxesDiscrepancies()
                            .findBoxesDiscrepanciesOfProduct(productInfo)
                            .filter {
                                it.boxNumber == taskBoxInfo.boxNumber
                            }.size
                    val countScannedExciseStamps = taskRepository
                            .getExciseStampsDiscrepancies()
                            .findExciseStampsDiscrepanciesOfProduct(productInfo)
                            .filter {
                                it.isScan
                                        && it.boxNumber == taskBoxInfo.boxNumber
                                        && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                            }.size

                    (countProcessedBoxes >= 1 && countScannedExciseStamps >= 1)
                            || (countScannedExciseStamps >= 2)
                            || (countScannedExciseStamps >= productInfo.numberStampsControl.toInt())
                }
                ?.size
                ?: 0
    }

    //контроль коробов для товара пройден
    fun controlBoxesOfProduct(productInfo: TaskProductInfo): Boolean { //https://trello.com/c/Z1SPfmAJ
        return countBoxesPassedControlOfProduct(productInfo) >= productInfo.numberBoxesControl.toInt()
    }

    //контроль марок в коробах для товара пройден
    fun controlExciseStampsOfProduct(productInfo: TaskProductInfo): Boolean { //https://trello.com/c/Z1SPfmAJ
        val countScannedExciseStampsDiscrepanciesOfProduct = taskRepository
                .getExciseStampsDiscrepancies()
                .findExciseStampsDiscrepanciesOfProduct(productInfo)
                .filter {
                    it.isScan
                            && it.materialNumber == productInfo.materialNumber
                            && it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }.size

        return controlBoxesOfProduct(productInfo)
                && countScannedExciseStampsDiscrepanciesOfProduct >= productInfo.numberStampsControl.toInt()
    }

    private fun changeProductDiscrepancyToBatch(productInfo: TaskProductInfo, batchDiscrepancies: TaskBatchesDiscrepancies) {
        val countOfDiscrepanciesOfProduct = taskRepository
                .getProductsDiscrepancies()
                .getCountOfDiscrepanciesOfProduct(
                        product = productInfo,
                        typeDiscrepancies = batchDiscrepancies.typeDiscrepancies
                )
        val countAdd = countOfDiscrepanciesOfProduct + batchDiscrepancies.numberDiscrepancies.toDouble()
        val foundDiscrepancy = taskRepository
                .getProductsDiscrepancies()
                .findProductDiscrepanciesOfProduct(productInfo)
                .findLast {
                    it.materialNumber == productInfo.materialNumber
                            && it.typeDiscrepancies == batchDiscrepancies.typeDiscrepancies
                }

        if (foundDiscrepancy == null) {
            taskRepository
                    .getProductsDiscrepancies()
                    .changeProductDiscrepancy(
                            TaskProductDiscrepancies(
                                    materialNumber = productInfo.materialNumber,
                                    processingUnitNumber = productInfo.processingUnit,
                                    numberDiscrepancies = countAdd.toString(),
                                    uom = productInfo.uom,
                                    typeDiscrepancies = batchDiscrepancies.typeDiscrepancies,
                                    isNotEdit = false,
                                    isNew = false,
                                    notEditNumberDiscrepancies = ""
                            )
                    )
        } else {
            taskRepository
                    .getProductsDiscrepancies()
                    .changeProductDiscrepancy(
                            foundDiscrepancy
                                    .copy(
                                            numberDiscrepancies = countAdd.toString()
                                    )
                    )
        }
    }
}



