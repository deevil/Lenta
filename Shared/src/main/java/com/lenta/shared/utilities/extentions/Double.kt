package com.lenta.shared.utilities.extentions

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
    return this.toString().dropLastWhile { it == '0' || it == '.' }
}