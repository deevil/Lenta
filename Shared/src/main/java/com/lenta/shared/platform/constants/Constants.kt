package com.lenta.shared.platform.constants

import android.os.Environment

object Constants {

    val DB_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/FMP/db"

    //---------Time formats---------//
    const val TIME_FORMAT_HHmm = "HH:mm"
    const val TIME_FORMAT_mmss = "mm:ss"
    const val TIME_FORMAT_hhmmss = "HH:mm:ss"
    const val TIME_FORMAT_LOGS = "yyyy-MM-dd_HH-mm-ss-SSS"
    const val CHECK_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    const val TIME_FORMAT_HH = "HH"
    const val TIME_FORMAT_mm = "mm"

    //---------Date formats---------//
    const val DATE_FORMAT_ddmmyy = "dd.MM.yy"
    const val DATE_FORMAT_ddmm = "dd.MM"
    const val DATE_FORMAT_yyyy_mm_dd = "yyyy-MM-dd"
    const val DATE_FORMAT_dd = "dd"
    const val DATE_FORMAT_mm = "MM"
    const val DATE_FORMAT_yy = "yy"

    // Other date formats
    const val DATE_TIME_ONE = "yyyy-MM-dd_HH:mm:ss"

    // Entered length
    const val COMMON_SAP_LENGTH = 6
    const val COMMON_SAP_FULL_LENGTH = 18
    const val SAP_OR_BAR_LENGTH = 12

}