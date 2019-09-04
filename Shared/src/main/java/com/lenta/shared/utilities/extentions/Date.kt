package com.lenta.shared.utilities.extentions

import com.lenta.shared.platform.constants.Constants
import java.text.SimpleDateFormat
import java.util.*

fun Date?.getFormattedDate(): String {
    return if (this != null) {
        SimpleDateFormat(Constants.DATE_FORMAT_ddmmyy, Locale.getDefault()).format(this)
    } else ""
}

fun Date?.getFormattedTime(): String {
    return if (this != null) {
        SimpleDateFormat(Constants.TIME_FORMAT_HHmm, Locale.getDefault()).format(this)
    } else ""
}