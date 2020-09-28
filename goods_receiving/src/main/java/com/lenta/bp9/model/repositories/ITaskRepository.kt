package com.lenta.bp9.model.repositories

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getProductsDiscrepancies(): ITaskProductsDiscrepanciesRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getExciseStampsDiscrepancies(): ITaskExciseStampDiscrepanciesRepository
    fun getExciseStampsBad(): ITaskExciseStampBadRepository
    fun getBoxesRepository(): ITaskBoxesRepository
    fun getBoxesDiscrepancies(): ITaskBoxesDiscrepanciesRepository
    fun getNotifications(): ITaskNotificationsRepository
    fun getReviseDocuments(): ITaskReviseDocumentsRepository
    fun getBatches(): ITaskBatchesRepository
    fun getBatchesDiscrepancies(): ITaskBatchesDiscrepanciesRepository
    fun getMercuryDiscrepancies(): ITaskMercuryDiscrepanciesRepository
    fun getSections():ITaskSectionRepository
    fun getDocumentsPrinting():ITaskDocumentsPrintingRepository
    fun getCargoUnits():ITaskCargoUnitsRepository
    fun getTransportMarriage():ITaskTransportMarriageRepository
    fun getBlocks():ITaskBlocksRepository
    fun getBlocksDiscrepancies(): ITaskBlocksDiscrepanciesRepository
    fun getZBatchesDiscrepancies(): ITaskZBatchesDiscrepanciesRepository
}