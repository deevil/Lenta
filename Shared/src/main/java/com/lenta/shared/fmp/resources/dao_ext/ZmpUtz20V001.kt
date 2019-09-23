package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz20V001
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo

fun ZmpUtz20V001.getAllReasonRejection(): List<ZmpUtz20V001.ItemLocal_ET_GRUNDS>? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_GRUNDS.all
}

fun List<ZmpUtz20V001.ItemLocal_ET_GRUNDS>.toReasonRejectionInfoList(): List<ReasonRejectionInfo> {
    return this.map {
        ReasonRejectionInfo(
                id = it.tid,
                qualityCode = it.grundcat,
                code = if (it.grundcat == "1") "1" else it.grund,
                name = if (it.grundcat == "1") "Норма" else it.gtext)
    }
}