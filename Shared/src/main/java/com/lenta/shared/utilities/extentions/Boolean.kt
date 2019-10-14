package com.lenta.shared.utilities.extentions

fun Boolean?.toSapBooleanString(): String {
    return if (this == true) "X" else ""
}