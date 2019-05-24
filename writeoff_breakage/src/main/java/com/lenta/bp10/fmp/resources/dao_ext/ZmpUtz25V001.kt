package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001

fun ZmpUtz25V001.getEanInfo(ean: String): ZmpUtz25V001.ItemLocal_ET_EANS? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("EAN = \"$ean\" LIMIT 1").getOrNull(0)
}

fun ZmpUtz25V001.getEanInfoFromMaterial(material: String?): ZmpUtz25V001.ItemLocal_ET_EANS? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" LIMIT 1").getOrNull(0)
}