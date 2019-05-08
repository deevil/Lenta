package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.fast.ZmpUtz32V001
import com.lenta.shared.utilities.extentions.toSQliteSet

fun ZmpUtz32V001.getMotionTypes(taskType: String, taskCntrlList: List<String>): Collection<ZmpUtz32V001.ItemLocal_ET_MOVREASONS> {
    return localHelper_ET_MOVREASONS
            .getWhere("TASK_TYPE == \"$taskType\" AND " +
                    "TASK_CNTRL IN ${taskCntrlList.toSQliteSet()}")
            .map {
                it.reason to it
            }.toMap().values
}