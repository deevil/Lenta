package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.*

class MemoryTaskRepository : ITaskRepository {

    private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()

    private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()

    private val taskNotificationsRepository: ITaskNotificationsRepository = MemoryTaskNotificationsRepository()

    private val taskReviseDocumentsRepository: ITaskReviseDocumentsRepository = MemoryTaskReviseDocumentsRepository()

    private val taskProductsDiscrepanciesRepository: ITaskProductsDiscrepanciesRepository = MemoryTaskProductsDiscrepanciesRepository()

    private val taskBatchesRepository: ITaskBatchesRepository = MemoryTaskBatchesRepository()

    private val taskBatchesDiscrepanciesRepository: ITaskBatchesDiscrepanciesRepository = MemoryTaskBatchesDiscrepanciesRepository()

    private val taskSectionRepository: ITaskSectionRepository = MemoryTaskSectionRepository()

    private val taskDocumentsPrintingRepository: ITaskDocumentsPrintingRepository = MemoryTaskDocumentsPrintingRepository()

    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }

    override fun getNotifications(): ITaskNotificationsRepository {
        return taskNotificationsRepository
    }

    override fun getReviseDocuments(): ITaskReviseDocumentsRepository {
        return taskReviseDocumentsRepository
    }

    override fun getProductsDiscrepancies(): ITaskProductsDiscrepanciesRepository {
        return taskProductsDiscrepanciesRepository
    }

    override fun getBatches(): ITaskBatchesRepository {
        return taskBatchesRepository
    }

    override fun getBatchesDiscrepancies(): ITaskBatchesDiscrepanciesRepository {
        return taskBatchesDiscrepanciesRepository
    }

    override fun getSections(): ITaskSectionRepository {
        return taskSectionRepository
    }

    override fun getDocumentsPrinting(): ITaskDocumentsPrintingRepository {
        return taskDocumentsPrintingRepository
    }
}