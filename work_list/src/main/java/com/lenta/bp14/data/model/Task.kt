package com.lenta.bp14.data.model


data class Task(
        val id: Int,
        val type: String,
        val name: String,
        val status: TaskStatus = TaskStatus.STARTED,
        val quantity: Int = 0
) {
}

enum class TaskStatus {
    STARTED,
    SELF_BLOCK,
    BLOCK
}