package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001

fun ZmpUtz30V001.getMaterial(material: String): ZmpUtz30V001.ItemLocal_ET_MATERIALS? {
    return localHelper_ET_MATERIALS.getWhere("MATERIAL = \"$material\" LIMIT 1").getOrNull(0)
}