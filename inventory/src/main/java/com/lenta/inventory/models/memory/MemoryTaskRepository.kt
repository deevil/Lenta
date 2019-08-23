package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskExciseStampRepository
import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.inventory.models.repositories.ITaskRepository
import com.lenta.inventory.models.repositories.ITaskStorePlaceRepository

class MemoryTaskRepository(
        private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository(),
        private val taskExciseStampRepository: MemoryTaskExciseStampRepository = MemoryTaskExciseStampRepository(),
        private val taskStorePlaceRepository: MemoryTaskStorePlaceRepository = MemoryTaskStorePlaceRepository()
) : ITaskRepository {


    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }

    override fun getStorePlace(): ITaskStorePlaceRepository {
        return taskStorePlaceRepository
    }
}