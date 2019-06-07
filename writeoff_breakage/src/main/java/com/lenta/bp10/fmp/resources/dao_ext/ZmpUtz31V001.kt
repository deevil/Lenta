package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.fast.ZmpUtz31V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001

@Suppress("INACCESSIBLE_TYPE", "UNREACHABLE_CODE")
fun ZmpUtz31V001.getDefaultReason(taskType: String, sectionId: String, material: ZmpUtz30V001.ItemLocal_ET_MATERIALS?): String {

    if (material == null) {
        return ""
    }

    with(localHelper_ET_WOBSECREASONS) {

        (getWhere("TASK_TYPE = \"$taskType\" AND " +
                "SECTION_ID = \"$sectionId\" AND " +
                "EKGRP=\"${material.ekgrp}\"  LIMIT 1").getOrNull(0)
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "SECTION_ID = \"$sectionId\" AND " +
                        "MATKL=\"${material.matkl}\"  LIMIT 1").getOrNull(0)
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "EKGRP = \"${material.ekgrp}\" AND " +
                        "MATKL = \"${material.matkl}\"  LIMIT 1").getOrNull(0)
                ?: getWhere("TASK_TYPE = \"$taskType\" AND " +
                        "SECTION_ID = \"$sectionId\" AND EKGRP = \"\"  AND MATKL = \"\" LIMIT 1").getOrNull(0))?.let {
            return it.reason
        }
    }

    return ""
}
