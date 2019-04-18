package com.lenta.bp10.models.task

class TaskType(val code: String,val name: String){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as TaskType

        return code == other?.code
    }
}
