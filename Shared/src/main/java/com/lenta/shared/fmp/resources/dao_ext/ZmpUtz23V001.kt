package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001

fun ZmpUtz23V001.getRetailType(storeNumber: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_WERKS_ADR.getWhere("WERKS = \"$storeNumber\"")
            .map { it.retailType }
            .firstOrNull()
}
