package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType

fun ZmpUtz30V001.getMaterial(material: String): ZmpUtz30V001.ItemLocal_ET_MATERIALS? {
    return localHelper_ET_MATERIALS.getWhere("MATERIAL LIKE '%$material' LIMIT 1").getOrNull(0)
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getMatrixType(): MatrixType {
    return when (matrType) {
        "A" -> MatrixType.Active
        "P" -> MatrixType.Passive
        "D" -> MatrixType.Deleted
        else -> MatrixType.Unknown
    }
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getSectionId(): Int {
    return abtnr.toIntOrNull() ?: 0
}

fun ZmpUtz30V001.ItemLocal_ET_MATERIALS.getProductType(): ProductType {
    if (isAlco.isNotEmpty()) {
        return if (isExc.isNotEmpty()) ProductType.ExciseAlcohol else ProductType.NonExciseAlcohol
    }
    return ProductType.General
}