package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz40V001

fun ZmpUtz40V001.getStorageList(taskType: String): List<String> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_LGORT_SRC.getWhere("TASK_TYPE = \"$taskType\"").mapNotNull { it.lgortSrc }
}