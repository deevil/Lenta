package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz38V001

fun ZmpUtz38V001.getIconDescriptionByCode(code: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ICONS.getWhere("CODE = \"$code\" LIMIT 1").getOrNull(0)?.text
}

fun ZmpUtz38V001.getIconDescription(iconCode: IconCode): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return getIconDescriptionByCode(iconCode.code)
}

enum class IconCode(val code: String) {
    EAN("01"),
    EXCISE_STAMP("02"),
    BOX_SCAN("03"),
    SECTION_NUMBER("04"),
    MATRIX_TYPE("05"),
    NOT_EXCISE_ALCO("06"),
    EXCISE_ALCO("07"),
    RETURN_POSSIBLE("08"),
    USUALY_PRODUCT("09"),
    TASK_WITH_ALCO("10"),
    QR_CODE("11"),
    MARKING_GOODS("14"),
    EXCEPTIONS_SHELF_LIFE("16"),
    GS1_CODE("23"),
    GS128_CODE("24"),
    ICON_FACT("25"),
    ICON_PLAN("26"),
    ICON_VET("27")
}

