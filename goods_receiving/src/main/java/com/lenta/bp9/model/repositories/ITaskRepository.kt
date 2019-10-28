package com.lenta.bp9.model.repositories

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getNotifications(): ITaskNotificationsRepository
    fun getReviseDocuments(): ITaskReviseDocumentsRepository
    fun getProductsDiscrepancies(): ITaskProductsDiscrepanciesRepository
    fun getBatches(): ITaskBatchesRepository
    fun getBatchesDiscrepancies(): ITaskBatchesDiscrepanciesRepository
    fun getSections():ITaskSectionRepository
    fun getDocumentsPrinting():ITaskDocumentsPrintingRepository
}