package com.lenta.bp9.model.repositories

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getNotifications(): ITaskNotificationsRepository
    fun getReviseDocuments(): ITaskReviseDocumentsRepository
    fun getProductsDiscrepancies(): ITaskProductsDiscrepancies
}