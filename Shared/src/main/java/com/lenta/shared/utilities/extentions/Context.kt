package com.lenta.shared.utilities.extentions

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter

fun Context.getDeviceIp(): String {
    val wm = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    @Suppress("DEPRECATION")
    return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
}