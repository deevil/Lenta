package com.lenta.bp14.models.work_list

import com.lenta.bp14.models.ITaskDescription

class WorkListTaskDescription(
        override val tkNumber: String,
        override val taskNumber: String,
        override var taskName: String,
        override val comment: String,
        override val description: String,
        override val isStrictList: Boolean
) : ITaskDescription