package com.lenta.shared.utilities.extentions

import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.util.*

fun Date?.getFormattedDate(): String {
    return this?.run {  DateTimeUtil.formatDate(this, Constants.DATE_FORMAT_ddmmyy) }.orEmpty()
}

fun Date?.getFormattedDateLongYear(): String {
    return this?.run {  DateTimeUtil.formatDate(this, Constants.DATE_FORMAT_dd_mm_yyyy) }.orEmpty()
}

fun Date?.getFormattedTime(): String {
    return this?.run {  DateTimeUtil.formatDate(this, Constants.TIME_FORMAT_HHmm) }.orEmpty()
}

fun Date?.getFormattedTimeForPriceTag(): String {
    return this?.run {  DateTimeUtil.formatDate(this, Constants.PRICE_TAG_DATE_TIME_ONE) }.orEmpty()
}

fun Date?.getFormattedDate(pattern: String): String {
    return this?.run {  DateTimeUtil.formatDate(this, pattern) }.orEmpty()
}