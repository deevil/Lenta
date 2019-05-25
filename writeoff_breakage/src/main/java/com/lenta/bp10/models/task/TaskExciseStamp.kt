package com.lenta.bp10.models.task

import com.lenta.shared.models.core.ExciseStamp

class TaskExciseStamp(materialNumber: String, code: String, val setMaterialNumber: String, val writeOffReason: String, val isBasStamp: Boolean) : ExciseStamp(materialNumber,code) {
    //setMaterialNumber - материал набора
    //writeOffReason - причина списания
    //isBasStamp - признак "плохой" марки

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TaskExciseStamp
        return equals(other)
    }

    fun equals(stamp: TaskExciseStamp?): Boolean {
        return if (stamp == null) {
            false
        } else stamp!!.materialNumber === materialNumber
                && stamp!!.code === code
                && stamp!!.setMaterialNumber === setMaterialNumber
                && stamp!!.writeOffReason === writeOffReason
                && stamp!!.isBasStamp == isBasStamp
    }
}