package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz39V001

fun ZmpUtz39V001.getTaskTypeList(): List<TaskType> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.all.map {
        TaskType(
                type = it.taskType,
                description = it.annotation,
                abtnr = it.divAbtnr,
                ekgrp = it.divEkgrp
        )
    }
}

data class TaskType(
        val type: String,
        val description: String,
        val abtnr: String,
        val ekgrp: String
)