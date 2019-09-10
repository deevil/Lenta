package com.lenta.bp14.models.data.pojo

import java.util.*

data class TaskFilter(
        val taskType: TaskType = TaskType.WORK_LIST,
        val goodName: String?,
        val sectionNumber: Int?,
        val goodsGroup: String?,
        val publicationDate: Date?
) {
}

enum class TaskType {
    WORK_LIST,
    PRICE_CHECK,
    CHECK_LIST,
    NOT_EXPOSED
}