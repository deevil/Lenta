package com.lenta.shared.utilities

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.lenta.shared.R
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun setIndicatorForTab(tabStrip: TabLayout?, tab: Int, color: TabIndicatorColor) {
    tabStrip?.let { tabLayout ->
        try {
            val tabItemLayout = (tabLayout.getChildAt(0) as LinearLayout).getChildAt(tab) as LinearLayout
            tabItemLayout.orientation = LinearLayout.HORIZONTAL
            val iconView = tabItemLayout.getChildAt(0) as ImageView
            val textView = tabItemLayout.getChildAt(1) as TextView
            tabItemLayout.removeView(iconView)

            val indicator = when (color) {
                TabIndicatorColor.YELLOW -> R.drawable.ic_indicator_tablayout_yellow_8dp
                TabIndicatorColor.RED -> R.drawable.ic_indicator_tablayout_red_8dp
            }

            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, indicator, 0)
            textView.compoundDrawablePadding = 5
        } catch (e: Exception) {
            Logg.w { "e: $e" }
        }
    }
}
@Throws(ParseException::class)
fun getDateFromString(date: String, pattern: String): Date {
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(date)
}

@Throws(RuntimeException::class)
fun getStringFromDate(date: Date, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

@Throws(RuntimeException::class)
fun getFormattedDate(date: String, sourcePattern: String, targetPattern: String): String {
    return getStringFromDate(getDateFromString(date, sourcePattern), targetPattern)
}

fun isCommonFormatNumber(number: String): Boolean {
    return ScanCodeInfo(number).isEnterCodeValid
}

fun getMaterialInCommonFormat(number: String): String {
    return "000000000000${number.takeLast(6)}"
}

fun isCigarettesMark(number: String) : Boolean {
    val cigarettesMarkPattern = Regex(Constants.CIGARETTES_MARK_PATTERN)
    return number.matches(cigarettesMarkPattern)
}

fun isCigarettesBox(number: String) : Boolean {
    val cigarettesBoxPattern = Regex(Constants.CIGARETTES_BOX_PATTERN)
    return number.matches(cigarettesBoxPattern)
}

fun isShoesMark(number: String) : Boolean {
    val shoesMarkPattern = Regex(Constants.SHOES_MARK_PATTERN)
    return number.matches(shoesMarkPattern)
}

fun String.getEan(): String {
    return if (startsWith("0")) drop(1) else this
}