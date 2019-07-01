package com.lenta.inventory.models.task

import com.lenta.inventory.models.task.InventoryTask

interface IProcessProductService {
    fun getTotalCount(): Double
    fun apply(): InventoryTask
    fun discard(): InventoryTask
}