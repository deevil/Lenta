package com.lenta.bp10.models.task

import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(
        material: String,
        markNumber: String,
        val setMaterialNumber: String = "",
        val writeOffReason: String,
        val boxNumber: String = "",
        val packNumber: String = "",
        val isBadStamp: Boolean = false
) : ExciseStamp(material, markNumber) {
    //setMaterialNumber - материал набора
    //writeOffReason - причина списания
    //isBadStamp - признак "плохой" марки

    fun copy(writeOffReason: String): TaskExciseStamp {
        return TaskExciseStamp(
                material = this.materialNumber,
                markNumber = this.code,
                setMaterialNumber = this.setMaterialNumber,
                writeOffReason = writeOffReason,
                boxNumber = this.boxNumber,
                packNumber = this.packNumber,
                isBadStamp = this.isBadStamp
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