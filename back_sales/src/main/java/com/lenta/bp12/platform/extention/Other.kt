package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.BlockType
import com.lenta.bp12.model.Category
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.pojo.PositionInfo
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.extentions.isSapTrue

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

fun PositionInfo.getCategory(goodKind: GoodKind): Category {
    val isExcise = goodKind == GoodKind.EXCISE
    val inner = this.innerQuantity.toDoubleOrNull() ?: 0.0

    return when {
        isExcise && inner <= 1 -> Category.MARK
        isExcise && inner > 1 -> Category.PART
        else -> Category.QUANTITY
    }
}

fun String.getControlType(): ControlType {
    return when(this) {
        "N" -> ControlType.COMMON
        "A" -> ControlType.ALCOHOL
        else -> ControlType.UNKNOWN
    }
}

fun String.getBlockType(): BlockType {
    return when (this) {
        "1" -> BlockType.SELF_LOCK
        "2" -> BlockType.LOCK
        else -> BlockType.UNLOCK
    }
}

fun String.addZerosToStart(targetLength: Int): String {
    var value = this
    while (value.length < targetLength) {
        value = "0$value"
    }

    return value
}