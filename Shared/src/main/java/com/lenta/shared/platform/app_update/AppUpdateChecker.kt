package com.lenta.shared.platform.app_update

import android.content.Context
import javax.inject.Inject
import kotlin.math.pow


class AppUpdateChecker @Inject constructor(private val context: Context) {

    fun isNeedUpdate(allowedAppVersion: String?, currentAppVersion: String? = null): Boolean {

        if (allowedAppVersion == null) {
            return false
        }

        val allowedVersionValue = getVersionValue(allowedAppVersion)
        val currentVersionValue = getVersionValue(currentAppVersion ?: getCurrentVersionAppName())

        return allowedVersionValue > currentVersionValue

    }

    fun getCurrentVersionAppName(): String {
        return context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
    }

    private fun getVersionValue(appVersion: String): Int {
        return appVersion.split(".").take(3).reversed().mapIndexed { index, part ->
            (((part.filter { char -> char.isDigit() }).toIntOrNull()
                    ?: 0) * (1000.toDouble().pow(index))).toInt()
        }.sumBy { it }
    }
}