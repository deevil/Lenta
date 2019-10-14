package com.lenta.shared.utilities.extentions

import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.date_time.DateTimeUtil
import java.util.*

fun Date?.getFormattedDate(): String {
    return if (this != null) {
        DateTimeUtil.formatDate(this, Constants.DATE_FORMAT_ddmmyy)
    } else ""
}

fun Date?.getFormattedTime(): String {
    return if (this != null) {
        DateTimeUtil.formatDate(this, Constants.TIME_FORMAT_HHmm)
    } else ""
}