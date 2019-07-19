package com.lenta.shared.utilities.extentions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.util.TypedValue

fun Context.getDeviceIp(): String {
    val wm = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    @Suppress("DEPRECATION")
    return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
}

private val outValue by lazy { TypedValue() }

fun Context.selectableItemBackgroundResId(): Int {
    theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
    return outValue.resourceId
}

fun Context.isWriteExternalStoragePermissionGranted(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}