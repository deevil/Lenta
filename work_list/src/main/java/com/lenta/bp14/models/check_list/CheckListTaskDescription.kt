package com.lenta.bp14.models.check_list

import com.lenta.bp14.models.ITaskDescription

data class CheckListTaskDescription(
        val tkNumber: String,
        override var taskName: String,
        override var comment: String,
        override var description: String
) : ITaskDescription