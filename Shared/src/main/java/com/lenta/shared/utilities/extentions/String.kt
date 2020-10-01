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

fun String.splitByLines(oneLineMaxLength: Int): List<String> {

    val lines = mutableListOf<String>()

    val line = StringBuilder()
    var prefix = ""

    this.split(" ").forEach { word ->
        if ((line.length + word.length + 1) > oneLineMaxLength) {
            lines.add(line.toString())
            line.clear()
            line.append(word)
        } else {
            line.append(prefix)
            line.append(word)
        }
        prefix = " "
    }

    lines.add(line.toString())

    return lines

}

fun String.getSapDate(pattern: String): Date? {
    return if (this != "0000-00-00" && this.isNotEmpty()) {
        try {
            DateTimeUtil.getDateFromString(this, pattern)
        } catch (e: Exception) {
            null
        }
    } else null
}

fun String.toDoubleWeight(): Double {
    return takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0
}