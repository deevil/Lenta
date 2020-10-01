package com.lenta.bp15.platform.extention

import com.lenta.bp15.model.pojo.GoodAdditionalInfo
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.getMatrixType

fun ZfmpUtz48V001.getGoodAdditionalInfo(material: String): GoodAdditionalInfo? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\" LIMIT 1").firstOrNull()?.let {
        GoodAdditionalInfo(
                name = it.name.orEmpty(),
                matrix = getMatrixType(it.matrType.orEmpty()),
                section = it.abtnr.orEmpty()
        )
    }
}