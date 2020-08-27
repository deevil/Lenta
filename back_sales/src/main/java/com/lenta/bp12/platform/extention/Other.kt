package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkRequestStatus
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import java.math.BigInteger

fun GoodKind.getDescriptionResId(): Int {
    return when (this) {
        GoodKind.COMMON -> R.string.common_product
        GoodKind.ALCOHOL -> R.string.alcohol
        GoodKind.EXCISE -> R.string.excise_alcohol
        GoodKind.MARK -> R.string.marked_good
    }
}

fun GoodInfoResult.getGoodKind(): GoodKind {
    val isAlcohol = this.materialInfo?.isAlcohol.isSapTrue()
    val isExcise = this.materialInfo?.isExcise.isSapTrue()
    val isMark = this.materialInfo?.markType.orEmpty().isNotEmpty()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        isMark -> GoodKind.MARK
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getMarkType(): MarkType {
    val markTypeString = materialInfo?.markType.orEmpty()
    return enumValueOrNull<MarkType>(markTypeString).orIfNull { MarkType.UNKNOWN }
}

fun MarkCartonBoxGoodInfoNetRequestResult.getMarkStatus(): MarkStatus {
    return when (this.markStatus) {
        MarkRequestStatus.MARK_FOUND -> MarkStatus.GOOD_MARK

        MarkRequestStatus.MARK_NOT_FOUND_IN_TASK,
        MarkRequestStatus.MARK_NOT_FOUND_OR_PROBLEMATIC,
        MarkRequestStatus.MARK_OF_DIFFERENT_GOOD -> MarkStatus.BAD_MARK

        MarkRequestStatus.CARTON_FOUND_OR_GRAYZONE -> MarkStatus.GOOD_CARTON

        MarkRequestStatus.CARTON_INCOMPLETE,
        MarkRequestStatus.CARTON_NOT_FOUND,
        MarkRequestStatus.CARTON_NOT_FOUND_IN_TASK,
        MarkRequestStatus.CARTON_OF_DIFFERENT_GOOD,
        MarkRequestStatus.CARTON_OLD -> MarkStatus.BAD_CARTON

        MarkRequestStatus.BOX_FOUND -> MarkStatus.GOOD_BOX

        else -> MarkStatus.BAD_BOX
    }
}

fun ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST.getGoodKind(): GoodKind {
    val isAlcohol = this.isAlco.isSapTrue()
    val isExcise = this.isExc.isSapTrue()
    val isMark = this.markType.orEmpty().isNotEmpty()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        isMark -> GoodKind.MARK
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getControlType(): ControlType {
    val isVet = this.materialInfo?.isVet.isSapTrue()
    val isAlcohol = this.materialInfo?.isAlcohol.isSapTrue()

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

fun String.extractAlcoCode(): String {
    return BigInteger(this.substring(7, 19), 36).toString().padStart(19, '0')
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

fun TaskType.isWholesaleType(): Boolean {
    return this.code == TypeCode.WHOLESALE.code
}