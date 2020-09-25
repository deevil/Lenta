package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository

class MemoryTaskRepository(
        private val taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository(),
        private val taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository(),
        private val taskWriteOfReasonRepository: ITaskWriteOffReasonRepository = MemoryTaskWriteOffReasonRepository()
) : ITaskRepository {

    override fun getProducts(): ITaskProductRepository {
        return taskProductRepository
    }

    override fun getExciseStamps(): ITaskExciseStampRepository {
        return taskExciseStampRepository
    }

    override fun getWriteOffReasons(): ITaskWriteOffReasonRepository {
        return taskWriteOfReasonRepository
    }

}