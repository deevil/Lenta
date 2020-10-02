package com.lenta.bp12.platform.extention

import com.lenta.bp12.platform.DEFAULT_DATE_LENGTH
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.constants.Constants.DIV_TO_RUB
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.orIfNull
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

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
 * Метод удаляет второй минус в строке
 * Использовать только если строка не проходит по регулярке Constants.STRING_WITH_ONLY_ONE_MINUS_IN_BEGINNING_PATTERN
 * */
fun String.deleteMinus(): String {
    val newString = this
    val indexOfLast = newString.indexOfLast { it == '-' }
    return if (indexOfLast >= 0) {
        buildString {
            append(newString.substring(0, indexOfLast))
            append(newString.substring(indexOfLast + 1, newString.length))
        }
    } else {
        newString
    }
}

fun String.extractAlcoCode(): String {
    return this.takeIf {
        it.length >= 19
    }?.run {
        BigInteger(this.substring(7, 19), 36).toString().padStart(19, '0')
    }.orIfNull { this }
}

fun String.convertMprToRub(): String {
    return this.toDoubleOrNull()?.div(DIV_TO_RUB).dropZeros()
}