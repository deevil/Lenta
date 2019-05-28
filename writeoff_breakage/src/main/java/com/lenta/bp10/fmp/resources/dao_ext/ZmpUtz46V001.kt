package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz46V001

fun ZmpUtz46V001.isSet(material: String): Boolean {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\"").isNotEmpty()
}

fun ZmpUtz46V001.getComponentsForSet(material: String): List<ZmpUtz46V001.ItemLocal_ET_SET_LIST> {
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\"")
}