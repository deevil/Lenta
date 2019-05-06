package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskWriteOffReasonRepository : ITaskWriteOffReasonRepository {

    private val arrWriteOffReason = ArrayList<TaskWriteOffReason>()

    override fun getWriteOffReasons(): List<TaskWriteOffReason> {
        return arrWriteOffReason
    }

    override fun findWriteOffReasonsOfProduct(product: ProductInfo): List<TaskWriteOffReason> {
        val foundWriteOffReason = ArrayList<TaskWriteOffReason>()
        for (i in arrWriteOffReason.indices) {
            if (product.materialNumber == arrWriteOffReason[i].materialNumber) {
                foundWriteOffReason.add(arrWriteOffReason[i])
            }
        }
        return foundWriteOffReason
    }

    override fun addWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean {
        var index = -1
        for (i in arrWriteOffReason.indices) {
            if (writeOffReason.materialNumber == arrWriteOffReason[i].materialNumber && writeOffReason.writeOffReason.code == arrWriteOffReason[i].writeOffReason.code) {
                index = i
            }
        }

        if (index == -1) {
            arrWriteOffReason.add(writeOffReason)
            return true
        }
        return false
    }

    override fun deleteWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean {
        var index = -1
        for (i in arrWriteOffReason.indices) {
            if (writeOffReason.materialNumber == arrWriteOffReason[i].materialNumber && writeOffReason.writeOffReason.code == arrWriteOffReason[i].writeOffReason.code) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        arrWriteOffReason.removeAt(index)
        return true
    }

    override fun deleteWriteOffReasonsForProduct(product: ProductInfo): Boolean {
        val delWriteOffReason = ArrayList<TaskWriteOffReason>()
        for (i in arrWriteOffReason.indices) {
            if (product.materialNumber == arrWriteOffReason[i].materialNumber) {
                delWriteOffReason.add(arrWriteOffReason[i])
            }
        }

        if (delWriteOffReason.isEmpty()) {
            return false
        }

        arrWriteOffReason.removeAll(delWriteOffReason)
        return true
    }

    override fun clear() {
        arrWriteOffReason.clear()
    }

    override fun get(index: Int): TaskWriteOffReason {
        return arrWriteOffReason[index]
    }

    override fun lenght(): Int {
        return arrWriteOffReason.size
    }
}