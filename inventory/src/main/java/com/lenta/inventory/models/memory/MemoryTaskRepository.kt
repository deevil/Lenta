package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskExciseStampRepository
import com.lenta.inventory.models.repositories.ITaskProductRepository
import com.lenta.inventory.models.repositories.ITaskRepository

class MemoryTaskRepository (private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository(),
                            private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()) : ITaskRepository {
    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }
}