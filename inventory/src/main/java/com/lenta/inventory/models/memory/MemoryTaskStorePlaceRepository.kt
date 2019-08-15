package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskStorePlaceRepository
import com.lenta.inventory.models.task.TaskStorePlaceInfo

class MemoryTaskStorePlaceRepository : ITaskStorePlaceRepository {

    private val storePlaceInfo: ArrayList<TaskStorePlaceInfo> = ArrayList()

    override fun getStorePlaces(): List<TaskStorePlaceInfo> {
        return storePlaceInfo.toList()
    }

    override fun findStorePlace(storePlace: TaskStorePlaceInfo): TaskStorePlaceInfo? {
        return findStorePlace(storePlace.placeCode)
    }

    override fun findStorePlace(storePlaceNumber: String): TaskStorePlaceInfo? {
        return storePlaceInfo.firstOrNull { it.placeCode == storePlaceNumber }
    }

    override fun updateStorePlaces(newStorePlaces: List<TaskStorePlaceInfo>) {
        newStorePlaces.forEach { newStore -> newStore.isProcessed = storePlaceInfo.find { it.placeCode == newStore.placeCode }?.isProcessed ?: false }
        val manuallyAdded = storePlaceInfo.filter { storePlace -> storePlace.addedManually && newStorePlaces.find { it.placeCode == storePlace.placeCode } == null }
        storePlaceInfo.clear()
        for (storePlace in newStorePlaces) {
            addStorePlace(storePlace)
        }
        for (storePlace in manuallyAdded) {
            addStorePlace(storePlace)
        }
    }

    override fun addStorePlace(storePlace: TaskStorePlaceInfo): Boolean {
        var updated = false
        val existingPlace = storePlaceInfo.find { storePlace.placeCode == it.placeCode }
        existingPlace?.let {
            storePlace.isProcessed = it.isProcessed
            storePlaceInfo.remove(it)
            updated = true
        }
        storePlaceInfo.add(storePlace)
        return !updated
    }

    override fun deleteStorePlace(storePlace: TaskStorePlaceInfo): Boolean {
        var index = -1
        for (i in storePlaceInfo.indices) {
            if (storePlace.placeCode == storePlaceInfo[i].placeCode) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }

        storePlaceInfo.removeAt(index)
        return true
    }

    override fun clear() {
        storePlaceInfo.clear()
    }
}