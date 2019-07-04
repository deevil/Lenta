package com.lenta.inventory.models.repositories

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getStorePlace(): ITaskStorePlaceRepository
}