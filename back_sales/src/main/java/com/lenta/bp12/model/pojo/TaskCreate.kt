package com.lenta.bp12.model.pojo

import com.lenta.shared.fmp.resources.dao_ext.ReturnReason
import com.lenta.shared.fmp.resources.dao_ext.TaskType

data class TaskCreate(
        val name: String,
        val type: TaskType,
        val storage: String,
        val reason: ReturnReason,
        val isAlcohol: Boolean,
        val isCommon: Boolean,
        val goods: MutableList<Good> = mutableListOf(),
        val baskets: MutableList<Basket> = mutableListOf()
)