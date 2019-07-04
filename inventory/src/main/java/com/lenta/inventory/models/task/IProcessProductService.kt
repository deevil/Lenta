package com.lenta.inventory.models.task

interface IProcessProductService {
    fun getTotalCount(): Double
    fun apply(): InventoryTask
    fun discard(): InventoryTask
}