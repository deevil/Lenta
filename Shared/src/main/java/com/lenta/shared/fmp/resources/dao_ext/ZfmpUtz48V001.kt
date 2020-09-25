package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.requests.combined.scan_info.pojo.ProductInfo

fun ZfmpUtz48V001.getProductInfo(material: String): List<ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\"")
}

fun ZfmpUtz48V001.getProductInfoByMaterial(material: String?): ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\" LIMIT 1").getOrNull(0)
}

fun ZfmpUtz48V001.getProductInfoByMatcode(matcode: String?): ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATCODE = \"$matcode\" LIMIT 1").getOrNull(0)
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.toMaterialInfo(): ProductInfo {
    return ProductInfo(
            material = material.orEmpty(),
            name = name.orEmpty(),
            matcode = matcode.orEmpty(),
            buom = buom.orEmpty()
    )
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getMatrixType(): MatrixType {
    return getMatrixType(matrType.orEmpty())
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getProductType(): ProductType {
    val isMarkedGood = markType?.isNotEmpty() == true || isMark?.isNotEmpty() == true
    return getProductType(isAlco?.isNotEmpty() == true, isExc?.isNotEmpty() == true, isMarkedGood)
}