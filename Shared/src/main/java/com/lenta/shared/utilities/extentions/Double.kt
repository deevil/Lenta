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