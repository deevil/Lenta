package com.lenta.shared.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ApplySharedPref")
class AppSettings @Inject constructor(
        val sharedPrefferences: SharedPreferences,
        val defaultConnectionSettings: DefaultConnectionSettings
) : IAppSettings {


    override var serverAddress: String
        get() = sharedPrefferences.getString("serverAddress", defaultConnectionSettings.serverAddress/*"http://9.6.24.110"*/)
        set(value) {
            sharedPrefferences.edit().putString("serverAddress", value).commit()
        }
    override var environment: String
        get() = sharedPrefferences.getString("environment", defaultConnectionSettings.environment/*"Lenta_LRQ"*/)
        set(value) {
            sharedPrefferences.edit().putString("environment", value).commit()
        }
    override var project: String
        get() = sharedPrefferences.getString("project", defaultConnectionSettings.project/*"PR_WOB"*/)
        set(value) {
            sharedPrefferences.edit().putString("project", value).commit()
        }

    override var lastLogin: String?
        get() = sharedPrefferences.getString("lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastLogin", value).commit()
        }

    override var lastTK: String?
        get() = sharedPrefferences.getString("lastTK", null)
        set(value) {
            sharedPrefferences.edit().putString("lastTK", value).commit()
        }

    override var lastPersonnelNumber: String?
        get() = sharedPrefferences.getString("lastPersonnelNumber", null)
        set(value) {
            sharedPrefferences.edit().putString("lastPersonnelNumber", value).commit()
        }

    override var lastPersonnelFullName: String?
        get() = sharedPrefferences.getString("lastPersonnelFullName", null)
        set(value) {
            sharedPrefferences.edit().putString("lastPersonnelFullName", value).commit()
        }


}

interface IAppSettings {
    var serverAddress: String
    var environment: String
    var project: String
    var lastLogin: String?
    var lastTK: String?
    var lastPersonnelNumber: String?
    var lastPersonnelFullName: String?

}

data class DefaultConnectionSettings(
        val serverAddress: String,
        val environment: String,
        val project: String
)