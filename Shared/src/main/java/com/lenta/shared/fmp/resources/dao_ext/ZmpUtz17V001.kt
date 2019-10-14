package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz17V001
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo

fun ZmpUtz17V001.getAllQuality(): List<ZmpUtz17V001.ItemLocal_ET_DICT>? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_DICT.all
}

fun List<ZmpUtz17V001.ItemLocal_ET_DICT>.toQualityInfoList(): List<QualityInfo> {
    return this.map {
        QualityInfo(
                id = it.tid,
                code = it.code,
                name = it.shtxt)
    }
}

fun ZmpUtz17V001.getItemsByTid(tid: String): MutableList<ZmpUtz17V001.ItemLocal_ET_DICT>? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_DICT.getWhere("TID = \"$tid\"")
}

fun List<ZmpUtz17V001.ItemLocal_ET_DICT>.toDescriptionsList(): List<String> {
    return this.map { it.shtxt }
}