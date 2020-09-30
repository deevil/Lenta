package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz41V001
import com.lenta.shared.fmp.resources.pojo.CheckParams

fun ZmpUtz41V001.isGoodAllowed(controlType: String, taskType: String, goodGroup: String? = "", purchaseGroup: String? = ""): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ALLOW_MATNR.getWhere("TASK_CNTRL = \"$controlType\" AND TASK_TYPE = \"$taskType\" AND MTART = \"$goodGroup\" AND EKGRP = \"$purchaseGroup\" LIMIT 1").isNotEmpty()
}

fun ZmpUtz41V001.getTaskAttributeList(taskType: String): Set<String> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ALLOW_MATNR.getWhere("TASK_TYPE = \"$taskType\"").mapNotNull { it.taskCntrl }.toSet()
}

fun ZmpUtz41V001.getAllParams(taskType: String): List<CheckParams> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ALLOW_MATNR.getWhere("TASK_TYPE = \"$taskType\"").mapNotNull {
        it.takeIf { it.taskCntrl != null && it.mtart != null && it.matkl != null && it.ekgrp != null }?.run {
            CheckParams(
                    controlType = it.taskCntrl.orEmpty(),
                    goodType = it.mtart.orEmpty(),
                    goodGroup = it.matkl.orEmpty(),
                    purchaseGroup = it.ekgrp.orEmpty()
            )
        }
    }
}