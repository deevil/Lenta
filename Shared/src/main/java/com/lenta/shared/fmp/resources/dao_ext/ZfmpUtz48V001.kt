package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
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
            material = material,
            name = name,
            matcode = matcode,
            buom = buom
    )
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getMatrixType(): MatrixType {
    return com.lenta.shared.models.core.getMatrixType(matrType)
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getProductType(): ProductType {
    return com.lenta.shared.models.core.getProductType(isAlco.isNotEmpty(), isExc.isNotEmpty())
}