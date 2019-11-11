package com.lenta.bp14.models.not_exposed

import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.requests.not_exposed_product.NotExposedTaskInfoResult

data class NotExposedTaskDescription(
        override val tkNumber: String,
        override val taskNumber: String,
        override var taskName: String,
        override val comment: String,
        override val description: String,
        override val isStrictList: Boolean,
        val additionalTaskInfo: NotExposedTaskInfoResult?
) : ITaskDescription