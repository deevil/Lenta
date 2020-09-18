package com.lenta.bp12.platform.extention

import com.lenta.bp12.R
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.platform.DEFAULT_QUANTITY
import com.lenta.bp12.request.pojo.CreateTaskBasketInfo
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkRequestStatus
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.getConvertedQuantity
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import java.math.BigInteger

fun GoodKind.getDescriptionResId(): Int {
    return when (this) {
        GoodKind.COMMON -> R.string.common_product
        GoodKind.ALCOHOL -> R.string.alcohol
        GoodKind.EXCISE -> R.string.excise_alcohol
        GoodKind.MARK -> R.string.marked_good
        GoodKind.VET -> R.string.vet_good
    }
}

fun GoodInfoResult.getGoodKind(): GoodKind {
    val isAlcohol = this.materialInfo?.isAlcohol.isSapTrue()
    val isExcise = this.materialInfo?.isExcise.isSapTrue()
    val isVet = this.materialInfo?.isVet.isSapTrue()
    val isMark = this.materialInfo?.markType.orEmpty().isNotEmpty()

    return when {
        isExcise -> GoodKind.EXCISE
        isAlcohol -> GoodKind.ALCOHOL
        isMark -> GoodKind.MARK
        isVet -> GoodKind.VET
        else -> GoodKind.COMMON
    }
}

fun GoodInfoResult.getMarkType(): MarkType {
    val markTypeString = materialInfo?.markType.orEmpty()
    return enumValueOrNull<MarkType>(markTypeString).orIfNull { MarkType.UNKNOWN }
}

fun CreateTaskBasketInfo.getMarkType(): MarkType {
    val markTypeString = marktypeGroup.orEmpty()
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

        MarkRequestStatus.BOX_NOT_FOUND,
        MarkRequestStatus.BOX_OF_DIFFERENT_GOOD,
        MarkRequestStatus.BOX_INCOMPLETE,
        MarkRequestStatus.BOX_NOT_FOUND_IN_TASK -> MarkStatus.BAD_BOX

        else -> MarkStatus.UNKNOWN
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
    val isMark = this.getMarkType() != MarkType.UNKNOWN

    return when {
        isMark -> ControlType.MARK
        !isAlcohol && !isVet -> ControlType.COMMON
        isAlcohol && !isVet -> ControlType.ALCOHOL
        !isAlcohol && isVet -> ControlType.VET
        isMark -> ControlType.MARK
        else -> ControlType.UNKNOWN
    }
}

fun TaskInfo.getControlType(): ControlType {
    val isVet = this.isVet.isSapTrue()
    val isAlcohol = this.isAlco.isSapTrue()
    val isUsual = this.isUsual.isSapTrue()
    val isMark = this.isMark.isSapTrue()

    return when {
        isUsual -> ControlType.COMMON
        isAlcohol -> ControlType.ALCOHOL
        isVet -> ControlType.VET
        isMark -> ControlType.MARK
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

fun ScanCodeInfo.getConvertedQuantityString(divider: Double): String {
    val converted = if (weight > 0.0) {
        getConvertedQuantity(divider)
    } else {
        DEFAULT_QUANTITY
    }
    return converted.dropZeros()
}