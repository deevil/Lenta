package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.Task

data class Task(
        var isProcessed: Boolean = false,
        val type: TaskType,
        val task: Task,
        var goods: List<Good>? = null
) {
}