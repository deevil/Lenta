package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz110V001
import com.lenta.shared.requests.combined.scan_info.pojo.GroupInfo

fun ZmpUtz110V001.getGroupName(groupName: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_GR_WEIGHT.getWhere("GRNAME = \"$groupName\"")
            .map { it.grname }
            .firstOrNull()
}

fun ZmpUtz110V001.getAllGroups(): List<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_GR_WEIGHT.all
}

fun ZmpUtz110V001.getGroupsToSelectedMarket(marketNumber: String): List<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_GR_WEIGHT.getWhere("WERKS_D = \"$marketNumber\"")
}

fun List<ZmpUtz110V001.ItemLocal_ET_GR_WEIGHT>.toGroupInfoList(): List<GroupInfo> {
    return this.map {
        GroupInfo(
                werks = it.werks.orEmpty(),
                number = it.grnum,
                name = it.grname
        )
    }

}