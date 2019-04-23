package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository

class MemoryTaskRepository(taskProductRepository: ITaskProductRepository, taskExciseStampRepository: ITaskExciseStampRepository, taskWriteOfReasonRepository: ITaskWriteOffReasonRepository) : ITaskRepository {

    private val taskProductRepository: ITaskProductRepository
    private val taskExciseStampRepository: ITaskExciseStampRepository
    private val taskWriteOfReasonRepository: ITaskWriteOffReasonRepository

    init {
        this.taskProductRepository = taskProductRepository
        this.taskExciseStampRepository = taskExciseStampRepository
        this.taskWriteOfReasonRepository = taskWriteOfReasonRepository
    }

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