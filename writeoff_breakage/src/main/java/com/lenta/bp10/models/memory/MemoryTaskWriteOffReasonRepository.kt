package com.lenta.bp10.models.memory

import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository
import com.lenta.bp10.models.task.TaskWriteOffReason
import com.lenta.shared.models.core.ProductInfo
import java.util.*

class MemoryTaskWriteOffReasonRepository(
        private val writeOffReasons: ArrayList<TaskWriteOffReason> = ArrayList()
) : ITaskWriteOffReasonRepository {

    override fun getWriteOffReasons(): List<TaskWriteOffReason> {
        return writeOffReasons
    }

    override fun findWriteOffReasonsOfProduct(product: ProductInfo): List<TaskWriteOffReason> {
        val foundWriteOffReason = ArrayList<TaskWriteOffReason>()
        for (i in writeOffReasons.indices) {
            if (product.materialNumber == writeOffReasons[i].materialNumber) {
                foundWriteOffReason.add(writeOffReasons[i])
            }
        }
        return foundWriteOffReason
    }

    override fun addWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean {
        var index = -1
        for (i in this.writeOffReasons.indices) {
            if (writeOffReason.materialNumber == this.writeOffReasons[i].materialNumber && writeOffReason.writeOffReason.code == this.writeOffReasons[i].writeOffReason.code) {
                index = i
            }
        }

        if (index == -1) {
            this.writeOffReasons.add(writeOffReason)
            return true
        }
        return false
    }

    override fun deleteWriteOffReason(writeOffReason: TaskWriteOffReason): Boolean {
        var index = -1
        for (i in this.writeOffReasons.indices) {
            if (writeOffReason.materialNumber == this.writeOffReasons[i].materialNumber && writeOffReason.writeOffReason.code == this.writeOffReasons[i].writeOffReason.code) {
                index = i
            }
        }

        if (index == -1) {
            return false
        }
        this.writeOffReasons.removeAt(index)
        return true
    }

    override fun deleteWriteOffReasonsForProduct(product: ProductInfo): Boolean {
        val delWriteOffReason = ArrayList<TaskWriteOffReason>()
        for (i in writeOffReasons.indices) {
            if (product.materialNumber == writeOffReasons[i].materialNumber) {
                delWriteOffReason.add(writeOffReasons[i])
            }
        }

        if (delWriteOffReason.isEmpty()) {
            return false
        }

        writeOffReasons.removeAll(delWriteOffReason)
        return true
    }

    override fun clear() {
        writeOffReasons.clear()
    }

    override fun get(index: Int): TaskWriteOffReason {
        return writeOffReasons[index]
    }

    override fun lenght(): Int {
        return writeOffReasons.size
    }

}