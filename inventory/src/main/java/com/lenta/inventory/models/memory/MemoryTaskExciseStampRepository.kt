package com.lenta.inventory.models.memory

import com.lenta.inventory.models.repositories.ITaskExciseStampRepository
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import java.util.*

class MemoryTaskExciseStampRepository(private val stamps: ArrayList<TaskExciseStamp> = ArrayList()) : ITaskExciseStampRepository {

    private var stampsSnapshot: List<TaskExciseStamp>? = null
    private var changedSinceSnapshot: Boolean = false

    override fun getExciseStamps(): List<TaskExciseStamp> {
        return stamps
    }

    override fun makeSnapshot() {
        stampsSnapshot = stamps.map { it.copy() }
        changedSinceSnapshot = false
    }

    override fun restoreSnapshot() {
        stampsSnapshot?.let { stampsList ->
            stamps.clear()
            stampsList.forEach { stamps.add(it) }
        }
        stampsSnapshot = null
        changedSinceSnapshot = false
    }

    override fun isChanged(): Boolean {
        return changedSinceSnapshot
    }

    override fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStamp> {
        return findExciseStampsOfProduct(product.materialNumber, product.placeCode, product.isSet)
    }

    override fun findExciseStampsOfProduct(materialNumber: String, storePlaceNumber: String, isSet: Boolean): List<TaskExciseStamp> {
        return stamps.filter { stamp ->
            (stamp.materialNumber == materialNumber && stamp.placeCode == storePlaceNumber) ||
                    (isSet && stamp.setMaterialNumber == materialNumber && stamp.placeCode == storePlaceNumber)
        }
    }

    override fun updateExciseStamps(newStamps: List<TaskExciseStamp>) {
        for (stamp in newStamps) {
            addExciseStamp(stamp)
        }
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
            changedSinceSnapshot = true
            return true
        }
        return false
    }

    override fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
        stamps.filter { taskExciseStamp ->
            exciseStamp.code == taskExciseStamp.code
        }.map {
            changedSinceSnapshot = true
            return stamps.remove(it)
        }
        return false
    }

    override fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean {
        (stamps.map { it }.filter { stamp ->
            if ((stamp.materialNumber == product.materialNumber && stamp.placeCode == product.placeCode) ||
                    (product.isSet && stamp.setMaterialNumber == product.materialNumber && stamp.placeCode == product.placeCode)) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

        }).let {
            changedSinceSnapshot = changedSinceSnapshot || it.isNotEmpty()
            return it.isNotEmpty()
        }
    }

    override fun addExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean {
        if (exciseStamps150.isEmpty()) {
            return false
        }

        changedSinceSnapshot = true
        val distinctStamp = ArrayList<TaskExciseStamp>()
        for (i in exciseStamps150.indices) {
            //убираем дубликаты
            if (!distinctStamp.contains(exciseStamps150[i])) {
                distinctStamp.add(exciseStamps150[i])
            }
        }

        stamps.removeAll(distinctStamp)
        stamps.addAll(distinctStamp)
        return true
    }

    override fun deleteExciseStamps(exciseStamps150: List<TaskExciseStamp>): Boolean {
        return stamps.removeAll(exciseStamps150)
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