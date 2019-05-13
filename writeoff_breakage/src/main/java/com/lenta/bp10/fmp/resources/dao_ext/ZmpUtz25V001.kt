package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz25V001

fun ZmpUtz25V001.getEanInfo(ean: String): ZmpUtz25V001.ItemLocal_ET_EANS? {
    return localHelper_ET_EANS.getWhere("EAN = \"$ean\" LIMIT 1").getOrNull(0)
}