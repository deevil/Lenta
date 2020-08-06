package com.lenta.shared.platform.constants

import android.os.Environment

object Constants {

    val DB_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath}/FMP/db"

    const val ONE_MINUTE_TIMEOUT = 60

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
    const val DATE_FORMAT_dd_mm_yyyy_hh_mm = "dd.MM.yyyy HH:mm"
    const val DATE_FORMAT_dd_mm_yyyy = "dd.MM.yyyy"
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
    const val SAP_6 = 6
    const val SAP_18 = 18
    const val SAP_OR_BAR_12 = 12
    const val BOX_26 = 26

    // Mark code length
    const val MARK_150 = 150
    const val MARK_68 = 68
    const val MARK_134 = 134 //Shoes
    const val MARK_39 = 39

    // Regex patterns
    const val GTIN_REGEX_PATTERN = "(0{6}(?<ean8gs1>\\d{8})|0(?<ean13gs1>\\d{13}))"
    const val TOBACCO_MARK_REGEX_PATTERN = "^01(0{6}(?<ean8gs1>\\d{8})|0(?<ean13gs1>\\d{13}))21(?<serialBlock>.{7})(8005(?<mrpBlock>\\d{6}))?|^(0{6}(?<ean8Pack>\\d{8})|0(?<ean13Pack>\\d{13}))(?<serialPack>.{7})(?<cryptoPack>.{8})$|(?<ean8>^\\d{8}$)|(?<ean13>^\\d{13}$)"
    const val SHOES_MARK_REGEX_PATTERN = "^01(?<gtin>\\d{14})21(?<serial>\\S{13})(?:240(?<tradeCode>\\d{4}))?(?:91(?<verificationKey>\\S{4}))?(?:92(?<verificationCode>\\S{88}))?$"

}