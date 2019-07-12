package com.lenta.inventory.models.repositories

import com.lenta.inventory.models.task.TaskStorePlaceInfo

interface ITaskStorePlaceRepository {
    fun getStorePlaces(): List<TaskStorePlaceInfo>
    fun findStorePlace(storePlace: TaskStorePlaceInfo): TaskStorePlaceInfo?
    fun findStorePlace(storePlaceNumber: String): TaskStorePlaceInfo?
    fun addStorePlace(storePlace: TaskStorePlaceInfo): Boolean
    fun updateStorePlaces(newStorePlaces: List<TaskStorePlaceInfo>)
    fun deleteStorePlace(storePlace: TaskStorePlaceInfo): Boolean
    fun clear()
}