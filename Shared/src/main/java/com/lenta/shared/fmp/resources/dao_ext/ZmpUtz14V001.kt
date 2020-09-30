package com.lenta.shared.fmp.resources.dao_ext

import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001


fun ZmpUtz14V001.getAllowedWobAppVersion(): String? {
    return getParams("WOB_ANDR_ALLOWED_V").firstOrNull()
}

fun ZmpUtz14V001.getAllowedPleAppVersion(): String? {
    return getParams("PLE_ANDR_ALLOWED_V").firstOrNull()
}

fun ZmpUtz14V001.getServerAddress(): String? {
    return getParams("PRO_WEIGHTADDRESS").firstOrNull()
}

fun ZmpUtz14V001.getProFillCondition(): String? {
    return getParams("PRO_FILL_CONT").firstOrNull()
}

fun ZmpUtz14V001.getIncludeCondition(): String? {
    return getParams("PRO_INCL_CONT").firstOrNull()
}

fun ZmpUtz14V001.getAllowedWklAppVersion(): String? {
    return getParams("WKL_ALLOWED_VRSN_A").firstOrNull()
}

fun ZmpUtz14V001.getAllowedProAppVersion(): String? {
    return getParams("PRO_ALLOWED_VRSN_A").firstOrNull()
}

fun ZmpUtz14V001.getAllowedBksAppVersion(): String? {
    return getParams("BKS_ALLOWED_VRSN_A").firstOrNull()
}

fun ZmpUtz14V001.getSelfControlPinCode(): String? {
    return getParams("PLE_INTERNAL_PINCODE").firstOrNull()
}

fun ZmpUtz14V001.getExternalAuditPinCode(): String? {
    return getParams("PLE_EXTERNAL_PINCODE").firstOrNull()
}

fun ZmpUtz14V001.getFacingsHyperParam(): String? {
    return getParams("PLE_COUNT_HM").firstOrNull()
}

fun ZmpUtz14V001.getFacingsSuperParam(): String? {
    return getParams("PLE_COUNT_SM").firstOrNull()
}

fun ZmpUtz14V001.getPlacesHyperParam(): String? {
    return getParams("PLE_CHECKEMPTY_HM").firstOrNull()
}

fun ZmpUtz14V001.getPlacesSuperParam(): String? {
    return getParams("PLE_CHECKEMPTY_SM").firstOrNull()
}

fun ZmpUtz14V001.getInvCountDuration(): String? {
    return getParams("INV_COUNT_DURATION").firstOrNull()
}

fun ZmpUtz14V001.getAutoExitTimeInMinutes(): Long? {
    return getParams("ALL_AUTOEXIT_TIMEOUT").firstOrNull()?.toLongOrNull()
}

fun ZmpUtz14V001.getAllowedMatTypesINV(): List<String> {
    return getParams("INV_MATTYPE_USED")
}

fun ZmpUtz14V001.getMaxPositionsProdWkl(): Double? {
    return getParams("WKL_MAX_PROD_QNT").getOrNull(0)?.toDoubleOrNull()
}

fun ZmpUtz14V001.getMaxAllowedPrintCopyWkl(): Int? {
    return getParams("WKL_MAX_COPY").getOrNull(0)?.toIntOrNull()
}

fun ZmpUtz14V001.getPrintServerMask(): String? {
    return getParams("WKL_PRNT_SERVER_MASK:").getOrNull(0)
}


fun ZmpUtz14V001.getGrsGrundPos(): String? {
    return getParams("GRS_GRUND_POS").firstOrNull()
}

fun ZmpUtz14V001.getGrsGrundNeg(): String? {
    return getParams("GRS_GRUND_NEG").firstOrNull()
}

fun ZmpUtz14V001.getGrzArriveBackDD(): String? {
    return getParams("GRZ_ARRIVE_BACK_DD").firstOrNull()
}

fun ZmpUtz14V001.getPcpContTimeMm(): String? {
    return getParams("PCP_CONT_TIME_MM").firstOrNull()
}

fun ZmpUtz14V001.getPcpExpirTimeMm(): String? {
    return getParams("PCP_EXPIR_TIME_MM").firstOrNull()
}

fun ZmpUtz14V001.getLabelLimit(): String? {
    return getParams("PRO_MAX_LBL_QNT").firstOrNull()
}

fun ZmpUtz14V001.getGrzUffMhdhb(): String? {
    return getParams("GRZ_UFF_MHDHB").firstOrNull()
}

fun ZmpUtz14V001.getGrwOlGrundcat(): String? {
    return getParams("GRW_OL_GRUNDCAT").firstOrNull()
}

fun ZmpUtz14V001.getGrwUlGrundcat(): String? {
    return getParams("GRW_UL_GRUNDCAT").firstOrNull()
}

fun ZmpUtz14V001.getGrzWerksOwnpr(): List<String>? {
    return getParams("GRZ_WERKS_OWNPR")
}

fun ZmpUtz14V001.getEoVolume(): Double? {
    return getParams("MVM_EO_VOLUME_KUBM").firstOrNull()?.toDoubleOrNull()
}

fun ZmpUtz14V001.getParamGrzRoundLackRatio(): String? {
    return getParams("GRZ_ROUND_LACK_RATIO").firstOrNull()
}

fun ZmpUtz14V001.getParamGrzRoundLackUnit(): String? {
    return getParams("GRZ_ROUND_LACK_UNIT").firstOrNull()
}

fun ZmpUtz14V001.getParamGrzRoundHeapRatio(): String? {
    return getParams("GRZ_ROUND_HEAP_RATIO").firstOrNull()
}

fun ZmpUtz14V001.getGrzCrGrundcat(): String? {
    return getParams("GRZ_CR_GRUNDCAT").firstOrNull()
}

fun ZmpUtz14V001.getGrzMeinsPack(): String? {
    return getParams("GRZ_MEINS_PACK").firstOrNull()
}

fun ZmpUtz14V001.getGrzExclGtin(): String? {
    return getParams("GRZ_EXCL_GTIN").firstOrNull()
}

fun ZmpUtz14V001.getGrzMarkRef(): String? {
    return getParams("GRZ_MARK_REF").firstOrNull()
}

fun ZmpUtz14V001.getGrzGrundMark(): String? {
    return getParams("GRZ_GRUND_MARK").firstOrNull()
}

fun ZmpUtz14V001.getGrzAlternMeins(): String? {
    return getParams("GRZ_ALTERN_MEINS").firstOrNull()
}

fun ZmpUtz14V001.getParamGrzPerishableHH(): String? {
    return getParams("GRZ_PERISHABLE_HH").firstOrNull()
}

fun ZmpUtz14V001.getSpecialTaskTypes(): List<String> {
    return getParams("WOB_SPEC_TASK_TYPE")
}

fun ZmpUtz14V001.getPerishableHh(): Int? {
    return getParams("GRZ_PERISHABLE_HH").getOrNull(0)?.toIntOrNull()
}

fun ZmpUtz14V001.getBKSBasketVolume(): Double? {
    return getParams("BKS_PALLET_VOL_KUBM").firstOrNull()?.toDoubleOrNull()
}

private fun ZmpUtz14V001.getParams(paramName: String): List<String> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_PARAMS.getWhere("PARAMNAME = \"$paramName\"").mapNotNull { it.paramvalue }
}
