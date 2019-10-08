package com.lenta.shared.utilities.extentions

import java.text.SimpleDateFormat
import java.util.*

fun Iterable<String>.toSQliteSet(): String {
    return this.joinToString(prefix = "(", separator = ",", postfix = ")") { "'$it'" }
}

fun String?.isSapTrue(): Boolean {
    return this == "X"
}

fun String?.getDate(pattern: String): Date? {
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
}