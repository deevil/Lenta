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
    const val DATE_FORMAT_dd_mm_yyyy_hh_mm = "dd.MM.yyyy  HH:mm"
    const val DATE_FORMAT_yyyy_mm_dd_hh_mm = "yyyyMMddHHmm"
    const val DATE_FORMAT_dd = "dd"
    const val DATE_FORMAT_mm = "MM"
    const val DATE_FORMAT_yy = "yy"
    const val DATE_FORMAT_yyyyMMdd = "yyyyMMdd"
    const val DATE_FORMAT_yyMMddhhmm = "yyMMddHHmm"

    // Other date formats
    const val DATE_TIME_ONE = "yyyy-MM-dd_HH:mm:ss"
    const val PRICE_TAG_DATE_TIME_ONE = "dd.MM.yyyy HH:mm"

    // Entered code length
    const val COMMON_SAP_LENGTH = 6
    const val COMMON_SAP_FULL_LENGTH = 18
    const val SAP_OR_BAR_LENGTH = 12

    // Mark code length
    const val EXCISE_FULL_CODE = 150
    const val EXCISE_SIMPLE_CODE = 68
    const val MARKED_FULL_CODE = 134
    const val MARKED_SIMPLE_CODE = 39

}