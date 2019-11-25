package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.ProcessingUnit

data class Task(
        var isProcessed: Boolean = false,
        val type: TaskType,
        val processingUnit: ProcessingUnit,
        var goods: List<Good>? = null
) {
}