package com.lenta.bp12.model.pojo

data class TaskType(
        val code: String,
        val description: String,
        val isDivBySection: Boolean,
        val isDivByPurchaseGroup: Boolean,
        val isDivByMark: Boolean,
        val isDivByGis: Boolean,
        val isDivByMinimalPrice: Boolean,
        val isDivByProvider: Boolean,
        val isDivByGoodType: Boolean
) {
    companion object {
        fun empty(): TaskType = TaskType(
                code = "",
                description = "",
                isDivBySection = false,
                isDivByPurchaseGroup = false,
                isDivByMark = false,
                isDivByGis = false,
                isDivByMinimalPrice = false,
                isDivByProvider = false,
                isDivByGoodType = false
        )
    }
}