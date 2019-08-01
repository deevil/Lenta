package com.lenta.shared.utilities.extentions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import android.util.TypedValue
import kotlin.system.exitProcess
import android.content.Intent
import com.lenta.shared.utilities.Logg
import java.lang.Exception
import android.content.pm.ApplicationInfo


fun Context.getDeviceIp(): String {
    val wm = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    @Suppress("DEPRECATION")
    return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
}


@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String {
    return Settings.Secure.getString(this.contentResolver, "android_id")
}

private val outValue by lazy { TypedValue() }

fun Context.selectableItemBackgroundResId(): Int {
    theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
    return outValue.resourceId
}

fun Context.isWriteExternalStoragePermissionGranted(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
}

fun Context.restartApp() {
    val packageManager = packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    this.startActivity(mainIntent)
    exitProcess(0)
}

fun Context.openAnotherApp(packageName: String) {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName.trim())
    if (launchIntent != null) {
        try {
            startActivity(launchIntent)
        } catch (e: Exception) {
            Logg.e { "e: $e" }
        }

    }
}

fun Context.getApplicationName(packageName: String): String {
    val pm = applicationContext.packageManager
    var applicationInfo: ApplicationInfo?
    try {
        applicationInfo = pm.getApplicationInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        applicationInfo = null
    }
    return (if (applicationInfo != null) pm.getApplicationLabel(applicationInfo) else "(unknown)") as String
}