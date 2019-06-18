package com.lenta.bp10.models.task

import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(materialNumber: String, code: String, val setMaterialNumber: String, val writeOffReason: String, val isBasStamp: Boolean) : ExciseStamp(materialNumber, code) {
    //setMaterialNumber - материал набора
    //writeOffReason - причина списания
    //isBasStamp - признак "плохой" марки


    fun copy(writeOffReason: String): TaskExciseStamp {
        return TaskExciseStamp(
                materialNumber = this.materialNumber,
                code = this.code,
                setMaterialNumber = this.setMaterialNumber,
                writeOffReason = writeOffReason,
                isBasStamp = this.isBasStamp
        )

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskExciseStamp

        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}