package com.lenta.bp12.model.pojo

import com.lenta.shared.fmp.resources.dao_ext.ReturnReason
import com.lenta.shared.fmp.resources.dao_ext.TaskType

data class CreateTask(
        val name: String,
        val type: TaskType,
        val storage: String,
        val reason: ReturnReason,
        val isAlcohol: Boolean,
        val isCommon: Boolean
)