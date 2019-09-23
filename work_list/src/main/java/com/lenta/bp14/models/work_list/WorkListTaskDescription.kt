package com.lenta.bp14.models.work_list

import com.lenta.bp14.models.ITaskDescription

class WorkListTaskDescription(
        val tkNumber: String,
        override val taskNumber: String,
        override var taskName: String,
        override val comment: String,
        override val description: String
) : ITaskDescription