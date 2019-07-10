package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskExciseStampRepository
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import java.util.*

class MemoryTaskExciseStampRepository : ITaskExciseStampRepository {

    private val stamps: ArrayList<TaskExciseStamp> = ArrayList()

    override fun getExciseStamps(): List<TaskExciseStamp> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStamp> {
        return findExciseStampsOfProduct(product.materialNumber, product.placeCode)
    }

    override fun findExciseStampsOfProduct(materialNumber: String, storePlaceNumber: String): List<TaskExciseStamp> {
        return stamps.filter { it.materialNumber == materialNumber && it.placeCode == storePlaceNumber}
    }

    override fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
        var index = -1
        for (i in stamps.indices) {
            if (exciseStamp.code == stamps[i].code) {
                index = i
            }
        }

        if (index == -1) {
            stamps.add(exciseStamp)
            return true
        }
        return false
    }

    override fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
        var index = -1
        for (i in stamps.indices) {
            if (exciseStamp.code == stamps[i].code) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        stamps.removeAt(index)
        return true
    }

    override fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean {
        (stamps.map { it.materialNumber }.filterIndexed { index, materialNumber ->
            if (materialNumber == product.materialNumber) {
                stamps.removeAt(index)
                return true
            }
            return false

        }).let {
            return it.isNotEmpty()
        }
    }

    override fun addExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean {
        if (exciseStamps150.isEmpty()) {
            return false
        }

        val distinctStamp = ArrayList<TaskExciseStamp>()
        for (i in exciseStamps150.indices) {
            //убираем дубликаты
            if (!distinctStamp.contains(exciseStamps150[i])) {
                distinctStamp.add(exciseStamps150[i])
            }
        }

        stamps.addAll(distinctStamp)
        return true
    }

    override fun deleteExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean {
        if (exciseStamps150.isEmpty()) {
            return false
        }

        stamps.removeAll(exciseStamps150)
        return true
    }

    override fun clear() {
        stamps.clear()
    }

    override fun get(index: Int): TaskExciseStamp {
        return stamps.get(index)
    }

    override fun lenght(): Int {
        return stamps.size
    }

    override fun containsStamp(code: String): Boolean {
        return getExciseStamps().firstOrNull { it.code == code } != null
    }
}