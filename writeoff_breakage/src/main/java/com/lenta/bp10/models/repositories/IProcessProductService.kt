package com.lenta.bp10.models.repositories

import com.lenta.bp10.models.task.WriteOffTask

interface IProcessProductService {
    fun getTotalCount(): Double
    fun apply(): WriteOffTask
    fun discard(): WriteOffTask
}