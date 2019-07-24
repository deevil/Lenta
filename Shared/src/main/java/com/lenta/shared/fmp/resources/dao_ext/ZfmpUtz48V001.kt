package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001

fun ZfmpUtz48V001.getProductInfo(material: String): List<ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\"")
}