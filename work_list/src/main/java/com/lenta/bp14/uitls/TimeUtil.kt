package com.lenta.bp14.uitls

import android.util.Log
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val SERVER_DATE_FORMAT = "yyyy-MM-dd"
private const val APPLICATION_DATE_FORMAT = "dd.MM.YY"
private const val EMPTY_INPUT_MESSAGE = "Input string equals none date"
private const val EMPTY_DATE = "0000-00-00"
private const val EMPTY_RETURN = ""

/**
 * Перевод времени из формата yyyy-MM-dd в dd.MM.YY
 *
 * @see SERVER_DATE_FORMAT
 * @see APPLICATION_DATE_FORMAT
 *
 * @return в случае если было передано время в формате SERVER_DATE_FORMAT - время переведенное в
 * APPLICATION_DATE_FORMAT иначе - EMPTY_RETURN
 * @see EMPTY_RETURN
 */
fun String.convertToApplicationTime(): String = try {
    if(this == EMPTY_DATE) throw IllegalArgumentException(EMPTY_INPUT_MESSAGE)
    val sdf = SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault())
    val date = sdf.parse(this)
    sdf.applyPattern(APPLICATION_DATE_FORMAT)
    sdf.format(date)
} catch (ex: Exception) {
    Log.w(javaClass.simpleName, "Bad time format, input: $this, pattern $SERVER_DATE_FORMAT, " +
            "\nException:${ex.message}")
    EMPTY_RETURN
}
