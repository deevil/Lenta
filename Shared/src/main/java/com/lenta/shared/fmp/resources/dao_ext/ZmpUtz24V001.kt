package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZmpUtz24V001
import com.lenta.shared.requests.combined.scan_info.pojo.MaterialInfo


fun ZmpUtz24V001.getGoodInfo(sapCode: String?): ZmpUtz24V001.ItemLocal_ET_MATERIALS? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATERIALS.getWhere("MATERIAL = \"$sapCode\" LIMIT 1").getOrNull(0)
}

fun ZmpUtz24V001.ItemLocal_ET_MATERIALS.toGoodInfo(): MaterialInfo {
    return MaterialInfo(
            material = material,
            name = name,
            matype = matype,
            buom = buom,
            mhdhbDays = mhdhbDays.toInt(),
            mhdrzDays = mhdrzDays.toInt(),
            bstme = bstme
    )
}