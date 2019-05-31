package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc

fun ZmpUtz29V001Rfc.isChkOwnpr(taskTypeCode: String): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.getWhere("TASK_TYPE = \"$taskTypeCode\" AND CHK_OWNPR=\"X\"").isNotEmpty()
}