package com.lenta.shared.platform.constants

import android.os.Environment

object Constants {

    val DB_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/FMP/db"

    const val ONE_MINUTE_TIMEOUT = 60

    // Time formats
    const val TIME_FORMAT_HHmm = "HH:mm"
    const val TIME_FORMAT_mmss = "mm:ss"
    const val TIME_FORMAT_hhmmss = "HH:mm:ss"
    const val TIME_FORMAT_LOGS = "yyyy-MM-dd_HH-mm-ss-SSS"
    const val CHECK_DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    const val TIME_FORMAT_HH = "HH"
    const val TIME_FORMAT_mm = "mm"

    // Date formats
    const val DATE_FORMAT_ddmmyy = "dd.MM.yy"
    const val DATE_FORMAT_ddmm = "dd.MM"
    const val DATE_FORMAT_yyyy_mm_dd = "yyyy-MM-dd"
    const val DATE_FORMAT_dd_mm_yyyy_hh_mm = "dd.MM.yyyy HH:mm"
    const val DATE_FORMAT_dd_mm_yyyy = "dd.MM.yyyy"
    const val DATE_FORMAT_yyyy_mm_dd_hh_mm = "yyyyMMddHHmm"
    const val DATE_FORMAT_dd = "dd"
    const val DATE_FORMAT_mm = "MM"
    const val DATE_FORMAT_yy = "yy"
    const val DATE_FORMAT_yyyyMMdd = "yyyyMMdd"
    const val DATE_FORMAT_yyMMddhhmm = "yyMMddHHmm"
    const val DATE_TIME_ONE = "yyyy-MM-dd_HH:mm:ss"
    const val PRICE_TAG_DATE_TIME_ONE = "dd.MM.yyyy HH:mm"

    // Entered sap or ean length
    const val SAP_6 = 6
    const val SAP_18 = 18
    const val SAP_OR_BAR_12 = 12

    // Mark code length
    const val MARK_134 = 134 // Эта марка используется в wkl для алкоголя, непонятно почему она такого размера...
    const val MARK_39 = 39 // Эта марка используется в wkl для алкоголя, непонятно почему она такого размера...
    const val EXCISE_MARK_150 = 150
    const val EXCISE_MARK_68 = 68
    const val EXCISE_BOX_26 = 26

    // Regex patterns
    const val CIGARETTES_MARK_PATTERN = "^(?<packBarcode>(?<gtin>\\d{14})(?<serial>\\S{7}))(?<MRC>\\S{4})(?:\\S{4})\\$"
    const val CIGARETTES_BOX_PATTERN = "^.?(?<blockBarcode>01(?<gtin2>\\d{14})21(?<serial>\\S{7})).?8005(?<MRC>\\d{6}).?93(?<verificationKey>\\S{4}).?(?<other>\\S{1,})?\\$"
    const val SHOES_MARK_PATTERN = "^(?<barcode>01(?<gtin>\\d{14})21(?<serial>\\S{13})).?(?:240(?<tradeCode>\\d{4}))?.?(?:91(?<verificationKey>\\S{4}))?.?(?:92(?<verificationCode>\\S{88}))?\\$"

    // ERP Requests
    const val OPERATING_SYSTEM_WINDOWS = "1"
    const val OPERATING_SYSTEM_ANDROID = "2"
    const val REQUEST_DEFAULT_FALSE = "X"

}