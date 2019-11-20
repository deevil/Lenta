package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskType

data class Task(
        val puNumber: String,
        val taskType: TaskType,
        var isProcessed: Boolean = false
) {
}