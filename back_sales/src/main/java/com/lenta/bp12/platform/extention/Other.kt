package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.extentions.isSapTrue
import java.math.BigInteger

fun GoodKind.getDescriptionResId(): Int {
    return when (this) {
        GoodKind.COMMON -> R.string.common_product
        GoodKind.ALCOHOL -> R.string.alcohol
        GoodKind.EXCISE -> R.string.excise_alcohol
    }
}

fun GoodInfoResult.getGoodKind(): GoodKind {
    val isAlcohol = this.materialInfo.isAlcohol.isSapTrue()
    val isExcise = this.materialInfo.isExcise.isSapTrue()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        else -> GoodKind.COMMON
    }
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getGoodKind(): GoodKind {
    val isAlcohol = this.isAlco.isSapTrue()
    val isExcise = this.isExc.isSapTrue()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getControlType(): ControlType {
    val isVet = this.materialInfo.isVet.isSapTrue()
    val isAlcohol = this.materialInfo.isAlcohol.isSapTrue()

    return when {
        !isAlcohol && !isVet -> ControlType.COMMON
        isAlcohol && !isVet -> ControlType.ALCOHOL
        else -> ControlType.UNKNOWN
    }
}

fun String.addZerosToStart(targetLength: Int): String {
    var value = this
    while (value.length < targetLength) {
        value = "0$value"
    }

    return value
}

fun ControlType.isCommon(): Boolean {
    return this == ControlType.COMMON
}

fun ControlType.isAlcohol(): Boolean {
    return this == ControlType.ALCOHOL
}

fun ControlType.isMark(): Boolean {
    return this == ControlType.MARK
}

fun String.extractAlcoCode(): String {
    return BigInteger(this.substring(7, 19), 36).toString().padStart(19, '0')
}