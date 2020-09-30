package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.requests.combined.scan_info.pojo.MarketInfo

fun ZmpUtz23V001.getRetailType(storeNumber: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_WERKS_ADR.getWhere("WERKS = \"$storeNumber\"")
            .mapNotNull { it.retailType }
            .firstOrNull()
}

fun ZmpUtz23V001.getAllMarkets(): List<ZmpUtz23V001.ItemLocal_ET_WERKS_ADR> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_WERKS_ADR.all
}

fun ZmpUtz23V001.getMarketByNumber(tkNumber: String): ZmpUtz23V001.ItemLocal_ET_WERKS_ADR? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_WERKS_ADR.getWhere("WERKS = \"$tkNumber\"").firstOrNull()
}

fun List<ZmpUtz23V001.ItemLocal_ET_WERKS_ADR>.toMarketInfoList(): List<MarketInfo> {
    return this.map { it.toMarketInfo() }
}

fun ZmpUtz23V001.ItemLocal_ET_WERKS_ADR.toMarketInfo(): MarketInfo {
    return MarketInfo(
            number = this.werks.orEmpty(),
            address = this.addres.orEmpty(),
            retailType = this.retailType.orEmpty(),
            version = this.version.orEmpty()
    )
}
