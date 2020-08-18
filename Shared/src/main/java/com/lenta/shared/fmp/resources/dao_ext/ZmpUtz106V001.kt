package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz106V001

fun ZmpUtz106V001.getWarehouseNumbers(tkNumber: String): List<ZmpUtz106V001.ItemLocal_ET_LGORT_LIST> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_LGORT_LIST.getWhere("WERKS = \"$tkNumber\"")
}