package com.lenta.bp9.model.task

import android.annotation.SuppressLint
import com.lenta.bp9.features.goods_information.z_batches.task_pge.ZBatchesInfoPGEViewModel
import com.lenta.bp9.model.repositories.ITaskRepository
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.Logg
import java.text.SimpleDateFormat
import java.util.*

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

    fun getProcessedZBatchesDiscrepancies(): List<TaskZBatchesDiscrepancies> {
        return taskRepository.getZBatchesDiscrepancies().getZBatchesDiscrepancies()
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

    fun getProcessedExciseStamps(): List<TaskExciseStampInfo> {
        return taskRepository.getExciseStamps().getExciseStamps()
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

    fun getProcessedTransportMarriage(): List<TaskTransportMarriageInfo> {
        return taskRepository.getTransportMarriage().getTransportMarriage()
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

        taskRepository
                .getMercuryDiscrepancies()
                .updateMercuryDiscrepancy(taskContentsInfo.taskMercuryDiscrepancies)
        /**для веттоваров таблица ET_TASK_DIFF приходит пустой, потому данные необходимо брать из таблицы ET_VET_DIFF,
         * чтобы на экранах Список товаров, Обнаружены расхождения и Информация о веттоваре, Детали по товару, были данные и их не переписывать под таблицу ET_VET_DIFF,
         * конвертируем и переносим данные из ET_VET_DIFF в ET_TASK_DIFF
         * */
        taskRepository
                .getProductsDiscrepancies()
                .addProductDiscrepancyOfMercuryDiscrepancy(taskContentsInfo.taskMercuryDiscrepancies)

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
        taskRepository
                .getZBatchesDiscrepancies()
                .updateZBatchesDiscrepancy(taskContentsInfo.taskZBatchesDiscrepancies)
        /**обновляем ZBatchesDiscrepancy, а также обновляем PartySignsOfZBatches, чтобы брать данные для экрана Список товаров в разрезе партий,
         * а именно, PartySignsTypeOfZBatches //партионный признак (ДП-дата производства, СГ-срок годности, изначально указываем ДП, если на карточке товара не было изменено),
         * и дату производста, которую расчитываем из срока годности, т.к. в ZBatch приходит только срок годности, а дата производства не приходит*/
        taskContentsInfo
                .taskZBatchesDiscrepancies
                .map {
                    taskRepository
                            .getZBatchesDiscrepancies()
                            .updatePartySignFromZBatch(it, getProductionDateForPartySignFromZBatch(it))
                }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getProductionDateForPartySignFromZBatch(zBatch: TaskZBatchesDiscrepancies): String {
        val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)
        val formatterEN = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)
        val productInfo = taskRepository.getProducts().findProduct(zBatch.materialNumber)
        val productGeneralShelfLife = productInfo?.generalShelfLife?.toInt() ?: 0
        val productMhdhbDays = productInfo?.mhdhbDays ?: 0
        val generalShelfLife: String

        generalShelfLife =
                if (productGeneralShelfLife > 0) {
                    productGeneralShelfLife.toString()
                } else {
                    productMhdhbDays.toString()
                }

        return try {
            if (zBatch.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) {
                val productionDate = Calendar.getInstance()
                val generalShelfLifeValue = generalShelfLife.toInt()
                productionDate.time = formatterEN.parse(zBatch.shelfLifeDate)
                productionDate.add(Calendar.DATE, -generalShelfLifeValue)
                productionDate.time?.let { formatterERP.format(it) }.orEmpty()
            } else {
                zBatch.shelfLifeDate
            }
        } catch (e: Exception) {
            Logg.e { "Get production date exception: $e" }
            ""
        }
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
                                it.isScan
                                        && it.boxNumber == taskBoxInfo.boxNumber
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
                .getCountOfDiscrepanciesOfProductOfProcessingUnit(
                        product = productInfo,
                        typeDiscrepancies = batchDiscrepancies.typeDiscrepancies,
                        processingUnitNumber = batchDiscrepancies.processingUnitNumber
                )
        val countAdd = countOfDiscrepanciesOfProduct + batchDiscrepancies.numberDiscrepancies.toDouble()
        val foundDiscrepancy = taskRepository
                .getProductsDiscrepancies()
                .findProductDiscrepanciesOfProductOfProcessingUnit(productInfo)
                .findLast { it.typeDiscrepancies == batchDiscrepancies.typeDiscrepancies }

        if (foundDiscrepancy == null) {
            taskRepository
                    .getProductsDiscrepancies()
                    .changeProductDiscrepancy(
                            TaskProductDiscrepancies(
                                    materialNumber = productInfo.materialNumber,
                                    processingUnitNumber = batchDiscrepancies.processingUnitNumber,
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



