package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.requests.combined.scan_info.pojo.EanInfo


fun ZmpUtz25V001.getEanInfo(ean: String?): ZmpUtz25V001.ItemLocal_ET_EANS? {
    if (ean == null) {
        return null
    }
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("EAN = \"$ean\" LIMIT 1").getOrNull(0)
}

fun ZmpUtz25V001.getEanInfoFromMaterial(material: String?): ZmpUtz25V001.ItemLocal_ET_EANS? {
    if (material == null) {
        return null
    }
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" LIMIT 1").getOrNull(0)
}

fun ZmpUtz25V001.getEanInfoByMaterialUnits(material: String, unitsCode: String): ZmpUtz25V001.ItemLocal_ET_EANS? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" AND UOM = \"${unitsCode.toUpperCase()}\" LIMIT 1").getOrNull(0)
}

fun ZmpUtz25V001.getEanListByMaterialUnits(material: String, unitsCode: String): List<String> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" AND UOM = \"${unitsCode.toUpperCase()}\"").map {
        it.ean
    }
}

fun ZmpUtz25V001.getMaterialByEan(ean: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("EAN = \"$ean\" LIMIT 1").firstOrNull()?.material
}

fun ZmpUtz25V001.getEansFromMaterial(material: String?): List<ZmpUtz25V001.ItemLocal_ET_EANS> {
    if (material == null) {
        return emptyList()
    }
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_EANS.getWhere("MATERIAL = \"$material\" ")
}

fun ZmpUtz25V001.isHaveEansForMaterial(material: String?): Boolean {
    return this.getEansFromMaterial(material).isNotEmpty()
}

fun ZmpUtz25V001.ItemLocal_ET_EANS.toEanInfo(): EanInfo {
    return EanInfo(
            ean = ean,
            materialNumber = material,
            umren = umren.toInt(),
            umrez = umrez.toInt(),
            uom = uom
    )
}