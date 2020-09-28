package com.lenta.bp10.models.task

data class TaskWriteOffReason(
        val writeOffReason: WriteOffReason,
        val materialNumber: String,
        val count: Double
)