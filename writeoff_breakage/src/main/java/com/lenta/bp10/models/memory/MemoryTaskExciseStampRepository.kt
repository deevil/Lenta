package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.task.TaskExciseStamp
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskExciseStampRepository : ITaskExciseStampRepository {

    private val stamps = ArrayList<TaskExciseStamp>()

    override fun getExciseStamps(): List<TaskExciseStamp> {
        return stamps
    }

    override fun findExciseStampsOfProduct(product: ProductInfo): List<TaskExciseStamp> {
        if (product == null) {
            throw NullPointerException("product")
        }

        val foundStamps = ArrayList<TaskExciseStamp>()
        for (i in stamps.indices) {
            if (product.materialNumber === stamps[i].materialNumber) {
                foundStamps.add(stamps[i])
            }
        }
        return foundStamps
    }

    override fun addExciseStamp(exciseStamp: TaskExciseStamp): Boolean {
        var index = -1
        for (i in stamps.indices) {
            // (Артем И., 05.04.2019) code (exciseStamp.getCode()) уникальный, поэтому сравнение делаем только по нему, по товару materialNumber не сравниваем
            if (exciseStamp.code === stamps[i].code) {
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
            if (exciseStamp.code === stamps[i].code) {
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
        val deleteStamps = ArrayList<TaskExciseStamp>()
        for (i in stamps.indices) {
            if (product.materialNumber === stamps[i].materialNumber) {
                deleteStamps.add(stamps[i])
            }
        }

        if (deleteStamps.isEmpty()) {
            return false
        }

        stamps.removeAll(deleteStamps)
        return true
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