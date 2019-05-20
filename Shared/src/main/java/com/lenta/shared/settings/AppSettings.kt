package com.lenta.shared.settings

import android.content.SharedPreferences
import javax.inject.Inject

class AppSettings @Inject constructor(
        val sharedPrefferences: SharedPreferences
) : IAppSettings {


    override var serverAddress: String
        get() = sharedPrefferences.getString("serverAddress", "http://9.6.24.110")
        set(value) {
            sharedPrefferences.edit().putString("serverAddress", value).commit()
        }
    override var environment: String
        get() = sharedPrefferences.getString("environment", "Lenta_LRQ")
        set(value) {
            sharedPrefferences.edit().putString("environment", value).commit()
        }
    override var project: String
        get() = sharedPrefferences.getString("project", "PR_WOB")
        set(value) {
            sharedPrefferences.edit().putString("project", value).commit()
        }


}

interface IAppSettings{
    var serverAddress: String
    var environment: String
    var project: String

}