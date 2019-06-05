package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001

fun ZmpUtz07V001.getUomInfo(uom: String?): ZmpUtz07V001.ItemLocal_ET_UOMS? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_UOMS.getWhere("UOM = \"$uom\" LIMIT 1").getOrNull(0)
}