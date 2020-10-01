package com.lenta.bp15.platform.extention

import com.lenta.bp15.repository.database.pojo.TaskType

fun getUnknownTaskType(): TaskType {
    return TaskType(
            code = "UTT",
            name = "Unknown task type",
            description = "Unknown task type"
    )
}