package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz42V001

fun ZmpUtz42V001.isGoodForbidden(gisControl: String, taskType: String, goodGroup: String? = "", purchaseGroup: String? = ""): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EXCLUDE_MATNR.getWhere("TASK_CNTRL = \"$gisControl\" AND TASK_TYPE = \"$taskType\" AND MTART = \"$goodGroup\" AND EKGRP = \"$purchaseGroup\" LIMIT 1").isNotEmpty()
}