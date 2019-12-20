package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.TaskInfo

data class Task(
        var isProcessed: Boolean = false,
        val number: String,
        var status: TaskStatus,
        val type: TaskType,
        val quantity: Int,
        val taskInfo: TaskInfo,
        var goods: List<Good>? = null
) {
}