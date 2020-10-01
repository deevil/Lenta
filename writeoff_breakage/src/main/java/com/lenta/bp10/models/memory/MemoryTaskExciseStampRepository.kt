package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskExciseStampRepository(
        private val stamps: ArrayList<TaskExciseStamp> = ArrayList()
) : ITaskExciseStampRepository {

    override fun getExciseStamps(): List<TaskExciseStamp> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: ProductInfo): List<TaskExciseStamp> {
        return findExciseStampsOfProduct(product.materialNumber)
    }

    override fun findExciseStampsOfProduct(materialNumber: String): List<TaskExciseStamp> {
        return stamps.filter { it.materialNumber == materialNumber || it.setMaterialNumber == materialNumber }
    }

    override fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
        var index = -1
        for (i in stamps.indices) {
            // (Артем И., 05.04.2019) code (exciseStamp.getCode()) уникальный, поэтому сравнение делаем только по нему, по товару materialNumber не сравниваем
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
            // (Артем И., 05.04.2019) code (exciseStamp.getCode()) уникальный, поэтому сравнение делаем только по нему, по товару materialNumber не сравниваем
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

    override fun deleteExciseStampsForProduct(product: ProductInfo): Boolean {
        (stamps.map { it.copy(writeOffReason = it.writeOffReason) }.filter { stamp ->
            if (stamp.materialNumber == product.materialNumber || (product.isSet && stamp.setMaterialNumber == product.materialNumber)) {
                stamps.remove(stamp)
                return@filter true
            }
            return@filter false

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
            /**if ( exciseStamps150.get(i).egaisVersion() != EgaisStampVersion_old.V3) {
             * throw new IllegalStateException("exciseStamps150 should contains only excise stamp with lenght " + String.valueOf(EgaisStampVersion_old.V3));
             * } */
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
}

fun ITaskExciseStampRepository.isContainsStamp(code: String): Boolean {
    val foundStamp = getExciseStamps().firstOrNull { stamp ->
        if (stamp.isBadStamp) {
            stamp.code.isNotEmpty() && stamp.code == code
        } else {
            stamp.code == code
        }
    }

    return foundStamp != null
}

fun ITaskExciseStampRepository.isContainsBox(boxNumber: String): Boolean {
    return getExciseStamps().firstOrNull { it.boxNumber == boxNumber } != null
}