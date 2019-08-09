package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampRepository
import com.lenta.bp9.model.repositories.ITaskProductRepository
import com.lenta.bp9.model.repositories.ITaskRepository

class MemoryTaskRepository : ITaskRepository {

    private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()

    private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()


    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }
}