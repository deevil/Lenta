package com.lenta.shared.utilities

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.lenta.shared.R
import java.text.SimpleDateFormat
import java.util.*

fun setIndicatorForTab(tabStrip: TabLayout?, tab: Int, color: TabIndicatorColor) {
    val tabItemLayout = (tabStrip?.getChildAt(0) as LinearLayout).getChildAt(tab) as LinearLayout
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
}

fun getDateFromString(date: String, pattern: String): Date {
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(date)
}

fun getStringFromDate(date: Date, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

fun getFormattedDate(date: String, sourcePattern: String, targetPattern: String): String {
    return getStringFromDate(getDateFromString(date, sourcePattern), targetPattern)
}