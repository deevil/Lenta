package com.lenta.bp12.platform.extention

import android.text.Editable
import com.lenta.bp12.R
import com.lenta.bp12.model.*
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.platform.DEFAULT_DATE_LENGTH
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.request.pojo.CreateTaskBasketInfo
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkCartonBoxGoodInfoNetRequestResult
import com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest.MarkRequestStatus
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.enumValueOrNull
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.getConvertedQuantity
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

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

fun TaskInfo.getControlTypes(): Set<ControlType> {
    val setOfControlTypes = mutableSetOf<ControlType>()

    setOfControlTypes add ControlType.VET ifTrue isVet.isSapTrue()
    setOfControlTypes add ControlType.ALCOHOL ifTrue isAlco.isSapTrue()
    setOfControlTypes add ControlType.COMMON ifTrue isUsual.isSapTrue()
    setOfControlTypes add ControlType.MARK ifTrue isMark.isSapTrue()

    return setOfControlTypes
}

fun CreateTaskBasketInfo.getControlType(): ControlType {
    val isVet = this.isVet.isSapTrue()
    val isAlcohol = this.isAlcohol.isSapTrue()
    val isMark = this.isMark.isSapTrue()
    val isCommon = this.isCommon.isSapTrue()

    return when {
        isMark -> ControlType.MARK
        isCommon -> ControlType.COMMON
        isAlcohol -> ControlType.ALCOHOL
        isVet -> ControlType.VET
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

/** Проверка даты на корректность
 * если дата в формате dd.mm.yyyy */
private fun String.isDateInFormatDdMmYyyyWithDotsCorrect(): Boolean {
    return if (this.isNotEmpty() && (this.length == DEFAULT_DATE_LENGTH)) {
        try {
            val splitCheckDate = this.split(".")
            val day = splitCheckDate[0].toInt()
            val month = splitCheckDate[1].toInt()
            val year = splitCheckDate[2].toInt()
            val monthWith31Days = listOf(1, 3, 5, 7, 8, 10, 12)
            val monthWith30Days = listOf(4, 6, 9, 11)
            when {
                (year < 1) || (year > 2100) -> false
                monthWith31Days.contains(month) -> day <= 31
                monthWith30Days.contains(month) && (month != 2) -> day <= 30
                (year % 4 == 0) -> day <= 29
                (month == 2) -> day <= 28
                else -> false
            }
        } catch (e: RuntimeException) {
            false
        }
    } else {
        false
    }
}

/** Проверка даты на корректность и что она не позже сегодняшней даты
 * если дата в формате dd.mm.yyyy */
fun String.isDateCorrectAndNotAfterToday(): Boolean {
    return if (this.isDateInFormatDdMmYyyyWithDotsCorrect()) {
        try {
            val date = SimpleDateFormat(
                    Constants.DATE_FORMAT_dd_mm_yyyy,
                    Locale.getDefault()
            ).parse(this)

            date <= Date()
        } catch (e: RuntimeException) {
            false
        }
    } else {
        false
    }
}

/**
 * Метод проверяет по регулярке есть ли второй минус в строке (учитывая что в строку может попасть только цифры и минус)
 * Использовать вместе с
 * TextViewBindingAdapter.AfterTextChanged
 * fun afterTextChanged(s: Editable?)
 * */
fun Editable?.returnWithNoSecondMinus(): String {
    val regex = Regex(Constants.STRING_WITH_ONLY_ONE_MINUS_IN_BEGINNING_PATTERN)
    val quantity = this.toString()
    return if (!quantity.matches(regex)) {
        quantity.deleteSecondMinus()
    } else {
        quantity
    }
}

/**
 * Метод удаляет второй минус в строке
 * Использовать только если строка не проходит по регулярке Constants.STRING_WITH_ONLY_ONE_MINUS_IN_BEGINNING_PATTERN
 * */
private fun String.deleteSecondMinus(): String {
    val newString = this
    val indexOfLast = newString.indexOfLast { it == '-' }
    return if (indexOfLast > 0) {
        buildString {
            append(newString.substring(0, indexOfLast))
            append(newString.substring(indexOfLast + 1, newString.length))
        }
    } else {
        newString
    }
}

fun String.extractAlcoCode(): String {
    return BigInteger(this.substring(7, 19), 36).toString().padStart(19, '0')
}


fun TaskType.isWholesaleType(): Boolean {
    return this.code == TypeCode.WHOLESALE.code
}

fun ScanCodeInfo.getConvertedQuantityString(divider: Double): String {
    val converted = if (weight > 0.0) {
        getConvertedQuantity(divider)
    } else {
        ZERO_QUANTITY
    }
    return converted.dropZeros()
}

fun <T> MutableCollection<T>.addIf(predicate: Boolean, whatToAdd: () -> T) {
    if (predicate) this.add(whatToAdd())
}

infix fun <T> MutableCollection<T>.add(whatToAdd: T): Holder<T> {
    return Holder(whatToAdd, this)
}

infix fun<T> Holder<T>.ifTrue(predictValue: Boolean) {
    if(predictValue) {
        this.who.add(this.value)
    }
}

data class Holder<T>(val value:T, val who:MutableCollection<T> )