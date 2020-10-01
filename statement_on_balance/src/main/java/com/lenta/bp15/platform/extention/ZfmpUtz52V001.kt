package com.lenta.bp15.platform.extention

import com.lenta.bp15.repository.database.pojo.TaskType
import com.lenta.shared.fmp.resources.fast.ZfmpUtz52V001

fun ZfmpUtz52V001.getTaskTypeList(): List<TaskType> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.all.map {
        TaskType(
                code = it.taskType,
                name = it.typeName,
                description = it.annotation
        )
    }
}

fun ZfmpUtz52V001.getTaskTypeByCode(taskTypeCode: String): TaskType? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.getWhere("TASK_TYPE = \"$taskTypeCode\" LIMIT 1").firstOrNull()?.let {
        TaskType(
                code = it.taskType,
                name = it.typeName,
                description = it.annotation
        )
    }
}