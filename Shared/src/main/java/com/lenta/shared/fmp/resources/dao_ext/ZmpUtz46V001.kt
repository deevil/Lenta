package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZmpUtz46V001

fun ZmpUtz46V001.isSet(material: String): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\" LIMIT 1").isNotEmpty()
}

fun ZmpUtz46V001.getComponentsForSet(material: String): List<ZmpUtz46V001.ItemLocal_ET_SET_LIST> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\"")
}