package com.lenta.bp14.platform.extentions


fun String?.toDoubleOrZero(): Double = this?.toDoubleOrNull() ?: 0.0