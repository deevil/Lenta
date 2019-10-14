package com.lenta.shared.utilities.extentions

import kotlin.math.roundToInt

fun Double?.toStringFormatted(): String {
    if (this == null) {
        return "0"
    }
    return if (this == this.toLong().toDouble())
        String.format("%d", this.toLong())
    else
        String.format("%s", this)
}

fun Double?.sumWith(other: Double?): Double {
    return ((this ?: 0.0).toBigDecimal() + (other ?: 0.0).toBigDecimal()).toDouble()
}

fun Double?.dropZeros(): String {
    return this.toStringFormatted()
}

fun Double.divideIntWithDecimal(): Pair<Int, Double> {
    val intPart = this.toInt()
    return Pair(intPart, this - intPart)
}


fun Double.divideRoubleWithKop(): Pair<String, String> {
    this.divideIntWithDecimal().let {
        return Pair(
                it.first.toString(),
                String.format("%02d", (it.second * 100).roundToInt())
        )

    }
}

fun Double?.toNullIfEmpty(): Double? {
    return if (this == null || this == 0.0) null else this
}
