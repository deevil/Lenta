package com.lenta.bp12.model.pojo

data class TaskType(
        val code: String,
        val description: String,
        val isDivBySection: Boolean,
        val isDivByPurchaseGroup: Boolean
)