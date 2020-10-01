package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz42V001
import com.lenta.shared.fmp.resources.pojo.CheckParams

fun ZmpUtz42V001.isGoodForbidden(gisControl: String, taskType: String, goodGroup: String? = "", purchaseGroup: String? = ""): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EXCLUDE_MATNR.getWhere("TASK_CNTRL = \"$gisControl\" AND TASK_TYPE = \"$taskType\" AND MTART = \"$goodGroup\" AND EKGRP = \"$purchaseGroup\" LIMIT 1").isNotEmpty()
}

fun ZmpUtz42V001.getAllParams(taskType: String): List<CheckParams> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EXCLUDE_MATNR.getWhere("TASK_TYPE = \"$taskType\"").mapNotNull {
        CheckParams(
                controlType = it.taskCntrl.orEmpty(),
                goodType = it.mtart.orEmpty(),
                goodGroup = it.matkl.orEmpty(),
                purchaseGroup = it.ekgrp.orEmpty()
        )
    }
}