package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.bp10.fmp.resources.slow.ZmpUtz46V001
import com.lenta.shared.utilities.extentions.toSQliteSet

fun ZmpUtz46V001.isSet(material: String): Boolean {
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\"").isNotEmpty()
}

fun ZmpUtz46V001.getComponentsForSet(material: String): List<ZmpUtz46V001.ItemLocal_ET_SET_LIST> {
    return localHelper_ET_SET_LIST.getWhere("MATNR_OSN = \"$material\"")
}

fun ZmpUtz30V001.getComponentsInfoForSet(materialComponents: List<String>): List<ZmpUtz30V001.ItemLocal_ET_MATERIALS> {
    return localHelper_ET_MATERIALS.getWhere("MATERIAL IN ${materialComponents.toSQliteSet()} ")
}