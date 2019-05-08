package com.lenta.bp10.models.task

data class TaskDescription(
        val taskType: TaskType,
        val taskName: String,
        val stock: String,
        val moveTypes: List<WriteOffReason>,
        val gisControls: List<String>,
        val materialTypes: List<String>,
        val perNo: String,
        val printer: String,
        val tkNumber: String,
        val ipAddress: String
)