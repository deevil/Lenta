package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001


fun ZmpUtz14V001.getAllowedWobAppVersion(): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return getParams("WOB_ALLOWED_VERSION").firstOrNull()
}

private fun ZmpUtz14V001.getParams(paramName: String): List<String> {
    return localHelper_ET_PARAMS.getWhere("PARAMNAME = \"$paramName\"").map { it.paramvalue }

}
