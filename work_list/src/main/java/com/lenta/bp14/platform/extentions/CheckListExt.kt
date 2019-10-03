package com.lenta.bp14.platform.extentions

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toCheckListGoodInfo(): CheckListGoodInfo? {
    return CheckListGoodInfo(
            material = material,
            name = name,
            buom = buom
    )
}

data class CheckListGoodInfo(
        val material: String,
        val name: String,
        val buom: String
)