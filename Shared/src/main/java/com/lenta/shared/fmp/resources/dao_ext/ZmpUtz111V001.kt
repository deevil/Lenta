package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz111V001
import com.lenta.shared.requests.combined.scan_info.pojo.ConditionInfo

fun ZmpUtz111V001.getConditionName(conditionName: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ST_COND.getWhere("STCONDNAM = \"$conditionName\"")
            .map { it.stcondnam }
            .firstOrNull()
}

fun ZmpUtz111V001.getAllConditions(): List<ZmpUtz111V001.ItemLocal_ET_ST_COND> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ST_COND.all
}

fun List<ZmpUtz111V001.ItemLocal_ET_ST_COND>.toConditionInfoList(): List<ConditionInfo> {
    return this.map {
            ConditionInfo(
                    werks = it.werks.orEmpty(),
                    matnr = it.matnr,
                    number = it.stcond,
                    name = it.stcondnam,
                    defCondition = it.defcond
            )
    }
}