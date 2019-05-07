package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.fast.ZmpUtz31V001

fun ZmpUtz31V001.getMotionTypes(taskType: String): Collection<ZmpUtz31V001.ItemLocal_ET_WOBSECREASONS> {
    return localHelper_ET_WOBSECREASONS
            .getWhere("TASK_TYPE == \"$taskType\"").map {
                it.reason to it
            }.toMap().values
}