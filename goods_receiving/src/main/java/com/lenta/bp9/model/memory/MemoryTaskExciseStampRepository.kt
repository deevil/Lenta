package com.lenta.bp9.model.memory

import com.lenta.bp9.model.task.TaskExciseStamp
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.repositories.ITaskExciseStampRepository

class MemoryTaskExciseStampRepository : ITaskExciseStampRepository {

    private val stamps: ArrayList<TaskExciseStamp> = ArrayList()

    override fun getExciseStamps(): List<TaskExciseStamp> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: TaskProductInfo): List<TaskExciseStamp> {
        return findExciseStampsOfProduct(product.materialNumber, product.isSet)
    }

    override fun findExciseStampsOfProduct(materialNumber: String, isSet: Boolean): List<TaskExciseStamp> {
        return stamps.filter {stamp ->
            (stamp.materialNumber == materialNumber) ||
                    (isSet && stamp.setMaterialNumber == materialNumber)}
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

    override fun addExciseStamps(exciseStamps: List<TaskExciseStamp>): Boolean {
        val distinctStamp = ArrayList<TaskExciseStamp>()
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

    override fun deleteExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
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

    override fun deleteExciseStamps(exciseStamps: List<TaskExciseStamp>): Boolean {
        return stamps.removeAll(exciseStamps)
    }

    override fun deleteExciseStampsForProduct(product: TaskProductInfo): Boolean {
        stamps.map { it }.filter {stamp ->
            if ((stamp.materialNumber == product.materialNumber) ||
                    (product.isSet && stamp.setMaterialNumber == product.materialNumber)) {
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