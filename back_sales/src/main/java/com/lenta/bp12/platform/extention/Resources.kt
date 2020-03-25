package com.lenta.bp12.platform.extention

import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.fmp.resources.slow.ZmpUtz09V001

fun ZmpUtz09V001.getProviderInfo(code: String): ProviderInfo? {
    var formattedCode = code
    while (formattedCode.length < 10) {
        formattedCode = "0$formattedCode"
    }

    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_VENDORS.getWhere("VENDOR = \"$formattedCode\" LIMIT 1").first()?.let { provider ->
        ProviderInfo(
               code = formattedCode,
                name = provider.vendorname
        )
    }
}