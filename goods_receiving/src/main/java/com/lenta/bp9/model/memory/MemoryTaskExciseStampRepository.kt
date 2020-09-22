package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskExciseStampRepository
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo

class MemoryTaskExciseStampRepository : ITaskExciseStampRepository {

    private val stamps: ArrayList<TaskExciseStampInfo> = ArrayList()

    override fun getExciseStamps(): List<TaskExciseStampInfo> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStampInfo> {
        return findExciseStampsOfProduct(product.materialNumber)
    }

    override fun findExciseStampsOfProduct(materialNumber: String): List<TaskExciseStampInfo> {
        return stamps.filter {stamp ->
            stamp.materialNumber == materialNumber
        }
    }

    override fun addExciseStamp(exciseStamp: TaskExciseStampInfo): Boolean {
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

    override fun addExciseStamps(exciseStamps: List<TaskExciseStampInfo>): Boolean {
        val distinctStamp = ArrayList<TaskExciseStampInfo>()
        for (i in exciseStamps.indices) {
            //убираем дубликаты
            if (!distinctStamp.contains(exciseStamps[i])) {
                distinctStamp.add(exciseStamps[i])
            }
        }

        stamps.removeAll(distinctStamp)
        stamps.addAll(distinctStamp)
        return true
    }

    override fun updateExciseStamps(newExciseStamps: List<TaskExciseStampInfo>) {
        clear()
        newExciseStamps.map {
            addExciseStamp(it)
        }
    }

    override fun changeExciseStamps(exciseStamp: TaskExciseStampInfo): Boolean {
        deleteExciseStamp(exciseStamp)
        return addExciseStamp(exciseStamp)
    }

    override fun deleteExciseStamp(exciseStamp: TaskExciseStampInfo): Boolean {
        stamps.map { it }.filter {stamp ->
            if (exciseStamp.code == stamp.code) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun deleteExciseStamps(exciseStamps: List<TaskExciseStampInfo>): Boolean {
        return stamps.removeAll(exciseStamps)
    }

    override fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean {
        stamps.map { it }.filter {stamp ->
            if (stamp.materialNumber == product.materialNumber || (product.isSet && stamp.setMaterialNumber == product.materialNumber)) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

        }.let {
            return it.isNotEmpty()
        }
    }

    override fun clear() {
        stamps.clear()
    }
}