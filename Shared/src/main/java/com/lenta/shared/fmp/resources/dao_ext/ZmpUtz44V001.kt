package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz44V001

fun ZmpUtz44V001.getReturnReasonList(taskType: String): List<ReturnReason> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_REASONS.getWhere("TASK_TYPE = \"$taskType\"").map {
        ReturnReason(
                code = it.reason,
                description = it.grtxt
        )
    }
}

data class ReturnReason(
        val code: String,
        val description: String
)