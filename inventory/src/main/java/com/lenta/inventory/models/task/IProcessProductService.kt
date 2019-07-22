package com.lenta.inventory.models.task

interface IProcessProductService {
    fun getFactCount(): Double
    fun setFactCount(count: Double)
    fun markMissing()
}