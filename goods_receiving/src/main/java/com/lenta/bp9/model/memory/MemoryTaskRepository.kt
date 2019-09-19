package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.*

class MemoryTaskRepository : ITaskRepository {


    private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()

    private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()

    private val taskNotificationsRepository: ITaskNotificationsRepository = MemoryTaskNotificationsRepository()

    private val taskReviseDocumentsRepository: ITaskReviseDocumentsRepository = MemoryTaskReviseDocumentsRepository()

    private val taskProductsDiscrepancies: ITaskProductsDiscrepancies = MemoryTaskProductsDiscrepanciesRepository()

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

    override fun getProductsDiscrepancies(): ITaskProductsDiscrepancies {
        return taskProductsDiscrepancies
    }
}