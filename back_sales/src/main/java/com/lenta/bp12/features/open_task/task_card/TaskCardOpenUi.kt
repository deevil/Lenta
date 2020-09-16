package com.lenta.bp12.features.open_task.task_card

data class TaskCardOpenUi(
        val name: String,
        val provider: String,
        val storage: String,
        val reason: String,
        val description: String,
        val comment: String,
        val isStrict: Boolean,
        val isAlcohol: Boolean,
        val isCommon: Boolean,
        val isMark: Boolean
)