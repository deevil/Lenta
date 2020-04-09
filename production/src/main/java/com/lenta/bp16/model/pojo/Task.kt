package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.TaskInfo

data class Task(
        var isProcessed: Boolean = false,
        val number: String,
        var status: TaskStatus,
        val isPack: Boolean,
        var isPackSent: Boolean = false,
        val type: TaskType,
        val quantity: Double,
        val taskInfo: TaskInfo,
        var goods: List<Good> = emptyList()
) {
}