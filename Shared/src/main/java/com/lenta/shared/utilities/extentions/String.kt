package com.lenta.shared.utilities.extentions

import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.util.*

fun Iterable<String>.toSQliteSet(): String {
    return this.joinToString(prefix = "(", separator = ",", postfix = ")") { "'$it'" }
}

fun String?.isSapTrue(): Boolean {
    return this == "X"
}

fun String.getDate(pattern: String): Date {
    return DateTimeUtil.getDateFromString(this, pattern)
}