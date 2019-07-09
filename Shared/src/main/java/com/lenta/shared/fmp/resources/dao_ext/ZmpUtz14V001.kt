package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001


fun ZmpUtz14V001.getAllowedWobAppVersion(): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return getParams("WOB_ANDR_ALLOWED_V").firstOrNull()
}

fun ZmpUtz14V001.getSelfControlPinCode(): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return getParams("PLE_INTERNAL_PINCODE").firstOrNull()
}

fun ZmpUtz14V001.getExternalAuditPinCode(): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return getParams("PLE_EXTERNAL_PINCODE").firstOrNull()
}

private fun ZmpUtz14V001.getParams(paramName: String): List<String> {
    return localHelper_ET_PARAMS.getWhere("PARAMNAME = \"$paramName\"").map { it.paramvalue }

}
