package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.*

class MemoryTaskRepository : ITaskRepository {

    private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()

    private val taskProductsDiscrepanciesRepository: ITaskProductsDiscrepanciesRepository = MemoryTaskProductsDiscrepanciesRepository()

    private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()

    private val taskExciseStampDiscrepanciesRepository: ITaskExciseStampDiscrepanciesRepository = MemoryTaskExciseStampDiscrepanciesRepository()

    private val taskExciseStampBadRepository: ITaskExciseStampBadRepository = MemoryTaskExciseStampBadRepository()

    private val taskBoxesRepository: ITaskBoxesRepository = MemoryTaskBoxesRepository()

    private val taskBoxesDiscrepanciesRepository: ITaskBoxesDiscrepanciesRepository = MemoryTaskBoxesDiscrepanciesRepository()

    private val taskNotificationsRepository: ITaskNotificationsRepository = MemoryTaskNotificationsRepository()

    private val taskReviseDocumentsRepository: ITaskReviseDocumentsRepository = MemoryTaskReviseDocumentsRepository()

    private val taskBatchesRepository: ITaskBatchesRepository = MemoryTaskBatchesRepository()

    private val taskBatchesDiscrepanciesRepository: ITaskBatchesDiscrepanciesRepository = MemoryTaskBatchesDiscrepanciesRepository()

    private val taskSectionRepository: ITaskSectionRepository = MemoryTaskSectionRepository()

    private val taskDocumentsPrintingRepository: ITaskDocumentsPrintingRepository = MemoryTaskDocumentsPrintingRepository()

    private val taskMercuryDiscrepanciesRepository: ITaskMercuryDiscrepanciesRepository = MemoryTaskMercuryDiscrepanciesRepository()

    private val taskCargoUnitsRepository: ITaskCargoUnitsRepository = MemoryTaskCargoUnitsRepository()

    private val taskTransportMarriageRepository: ITaskTransportMarriageRepository = MemoryTaskTransportMarriageRepository()

    private val taskBlocksRepository: ITaskBlocksRepository = MemoryTaskBlocksRepository()

    private val taskBlocksDiscrepanciesRepository: ITaskBlocksDiscrepanciesRepository = MemoryTaskBlocksDiscrepanciesRepository()

    private val taskZBatchesDiscrepanciesRepository: ITaskZBatchesDiscrepanciesRepository = MemoryTaskZBatchesDiscrepanciesRepository()

    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getProductsDiscrepancies(): ITaskProductsDiscrepanciesRepository {
        return taskProductsDiscrepanciesRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }

    override fun getExciseStampsDiscrepancies(): ITaskExciseStampDiscrepanciesRepository {
        return taskExciseStampDiscrepanciesRepository
    }

    override fun getExciseStampsBad(): ITaskExciseStampBadRepository {
        return taskExciseStampBadRepository
    }

    override fun getBoxesRepository(): ITaskBoxesRepository {
        return taskBoxesRepository
    }

    override fun getBoxesDiscrepancies(): ITaskBoxesDiscrepanciesRepository {
        return taskBoxesDiscrepanciesRepository
    }

    override fun getNotifications(): ITaskNotificationsRepository {
        return taskNotificationsRepository
    }

    override fun getReviseDocuments(): ITaskReviseDocumentsRepository {
        return taskReviseDocumentsRepository
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

    override fun getMercuryDiscrepancies(): ITaskMercuryDiscrepanciesRepository {
        return taskMercuryDiscrepanciesRepository
    }

    override fun getCargoUnits(): ITaskCargoUnitsRepository {
        return taskCargoUnitsRepository
    }

    override fun getTransportMarriage(): ITaskTransportMarriageRepository {
        return taskTransportMarriageRepository
    }

    override fun getBlocks(): ITaskBlocksRepository {
        return taskBlocksRepository
    }

    override fun getBlocksDiscrepancies(): ITaskBlocksDiscrepanciesRepository {
        return taskBlocksDiscrepanciesRepository
    }

    override fun getZBatchesDiscrepancies(): ITaskZBatchesDiscrepanciesRepository {
        return taskZBatchesDiscrepanciesRepository
    }
}