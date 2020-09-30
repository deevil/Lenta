package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.fast.ZmpUtz31V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001

@Suppress("INACCESSIBLE_TYPE", "UNREACHABLE_CODE")
fun ZmpUtz31V001.getDefaultReason(taskType: String, sectionId: String, material: ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST?): String {

    if (material == null) {
        return ""
    }

    with(localHelper_ET_WOBSECREASONS) {

        (getWhere("TASK_TYPE = \"$taskType\" AND " +
                "SECTION_ID = \"$sectionId\" AND " +
                "EKGRP=\"${material.ekgrp.orEmpty()}\"  LIMIT 1").firstOrNull()
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "SECTION_ID = \"$sectionId\" AND " +
                        "MATKL=\"${material.matkl.orEmpty()}\"  LIMIT 1").firstOrNull()
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "EKGRP = \"${material.ekgrp.orEmpty()}\" AND " +
                        "MATKL = \"${material.matkl.orEmpty()}\"  LIMIT 1").firstOrNull()
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "SECTION_ID = \"$sectionId\" AND EKGRP = \"\"  AND MATKL = \"\" LIMIT 1").firstOrNull())?.let {
            return it.reason.orEmpty()
        }
    }

    return ""
}

@Suppress("INACCESSIBLE_TYPE", "UNREACHABLE_CODE")
fun ZmpUtz31V001.getReasonPosition(taskType: String, sectionId: String): String {

    with(localHelper_ET_WOBSECREASONS) {
        val item =
                getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "SECTION_ID = \"$sectionId\" LIMIT 1").firstOrNull()

        return item?.reason.orEmpty()
    }

    return ""
}
