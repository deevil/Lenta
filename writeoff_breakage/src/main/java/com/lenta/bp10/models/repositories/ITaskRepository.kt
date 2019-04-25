package com.lenta.bp10.models.repositories

interface ITaskRepository {
    fun getProducts(): ITaskProductRepository
    fun getExciseStamps(): ITaskExciseStampRepository
    fun getWriteOffReasons(): ITaskWriteOffReasonRepository
}