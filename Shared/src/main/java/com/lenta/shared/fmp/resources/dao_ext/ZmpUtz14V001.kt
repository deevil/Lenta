package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001


fun ZmpUtz14V001.getAllowedWobAppVersion(): String? {
    return getParams("WOB_ANDR_ALLOWED_V").firstOrNull()
}

fun ZmpUtz14V001.getSelfControlPinCode(): String? {
    return getParams("PLE_INTERNAL_PINCODE").firstOrNull()
}

fun ZmpUtz14V001.getExternalAuditPinCode(): String? {
    return getParams("PLE_EXTERNAL_PINCODE").firstOrNull()
}

fun ZmpUtz14V001.getFacingsHyperParam(): String? {
    return getParams("PLE_COUNT_HM").firstOrNull()
}

fun ZmpUtz14V001.getFacingsSuperParam(): String? {
    return getParams("PLE_COUNT_SM").firstOrNull()
}

fun ZmpUtz14V001.getPlacesHyperParam(): String? {
    return getParams("PLE_CHECKEMPTY_HM").firstOrNull()
}

fun ZmpUtz14V001.getPlacesSuperParam(): String? {
    return getParams("PLE_CHECKEMPTY_SM").firstOrNull()
}

private fun ZmpUtz14V001.getParams(paramName: String): List<String> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_PARAMS.getWhere("PARAMNAME = \"$paramName\"").map { it.paramvalue }

}
