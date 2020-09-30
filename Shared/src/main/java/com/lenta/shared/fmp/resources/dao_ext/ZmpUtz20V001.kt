package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz20V001
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo

fun ZmpUtz20V001.getAllReasonRejection(): List<ZmpUtz20V001.ItemLocal_ET_GRUNDS>? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_GRUNDS.all
}

fun List<ZmpUtz20V001.ItemLocal_ET_GRUNDS>.toReasonRejectionInfoList(): List<ReasonRejectionInfo> {
    return this.mapNotNull {
        it.takeIf { it.tid != null }?.run {
            ReasonRejectionInfo(
                    id = tid.orEmpty(),
                    qualityCode = grundcat.orEmpty(),
                    code = if (grundcat == "1") "1" else grund.orEmpty(),
                    name = if (grundcat == "1") "Норма" else gtext.orEmpty()
            )
        }
    }
}