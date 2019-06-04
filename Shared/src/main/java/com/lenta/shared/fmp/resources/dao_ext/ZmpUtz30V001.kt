package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType

fun ZmpUtz30V001.getMaterial(material: String): ZmpUtz30V001.ItemLocal_ET_MATERIALS? {
    return localHelper_ET_MATERIALS.getWhere("MATERIAL LIKE '%$material' LIMIT 1").getOrNull(0)
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getMatrixType(): MatrixType {
    return com.lenta.shared.models.core.getMatrixType(matrType)
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getSectionId(): Int {
    return abtnr.toIntOrNull() ?: 0
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getProductType(): ProductType {
    return com.lenta.shared.models.core.getProductType(isAlco.isNotEmpty(), isExc.isNotEmpty())
}