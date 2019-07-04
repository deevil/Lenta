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

    override fun addStorePlace(storePlace: TaskStorePlaceInfo): Boolean {
        var index = -1
        for (i in storePlaceInfo.indices) {
            if (storePlace.placeCode == storePlaceInfo[i].placeCode) {
                index = i
            }
        }

        if (index == -1) {
            storePlaceInfo.add(storePlace)
            return true
        } else if (index !=  storePlaceInfo.size - 1) {
            storePlaceInfo.getOrNull(index)?.let {
                storePlaceInfo.removeAt(index)
                storePlaceInfo.add(it)
            }

        }

        return false
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