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

    override var isTest: Boolean
        get() = sharedPrefferences.getBoolean("isTest", false)
        set(value) {
            sharedPrefferences.edit().putBoolean("isTest", value).commit()
        }

    override var serverAddress: String
        get() = sharedPrefferences.getString("serverAddress", defaultConnectionSettings.serverAddress)
        set(value) {
            sharedPrefferences.edit().putString("serverAddress", value).commit()
        }
    override var environment: String
        get() = sharedPrefferences.getString("environment", defaultConnectionSettings.environment)
        set(value) {
            sharedPrefferences.edit().putString("environment", value).commit()
        }
    override var project: String
        get() = sharedPrefferences.getString("project", defaultConnectionSettings.project)
        set(value) {
            sharedPrefferences.edit().putString("project", value).commit()
        }

    override var testServerAddress: String
        get() = sharedPrefferences.getString("testServerAddress", defaultConnectionSettings.testServerAddress)
        set(value) {
            sharedPrefferences.edit().putString("testServerAddress", value).commit()
        }
    override var testEnvironment: String
        get() = sharedPrefferences.getString("testEnvironment", defaultConnectionSettings.testEnvironment)
        set(value) {
            sharedPrefferences.edit().putString("testEnvironment", value).commit()
        }
    override var testProject: String
        get() = sharedPrefferences.getString("testProject", defaultConnectionSettings.testProject)
        set(value) {
            sharedPrefferences.edit().putString("testProject", value).commit()
        }

    override var techLogin: String
        get() = sharedPrefferences.getString("techLogin", defaultConnectionSettings.testEnvironment)
        set(value) {
            sharedPrefferences.edit().putString("techLogin", value).commit()
        }

    override var techPassword: String
        get() = sharedPrefferences.getString("techPassword", defaultConnectionSettings.testEnvironment)
        set(value) {
            sharedPrefferences.edit().putString("techPassword", value).commit()
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

    override fun getCurrentServerAddress(): String {
        return if (isTest) testServerAddress else serverAddress
    }

    override fun getCurrentEnvironment(): String {
        return if (isTest) testEnvironment else environment
    }

    override fun getCurrentProject(): String {
        return if (isTest) testProject else project
    }


}

interface IAppSettings {
    var isTest: Boolean
    var serverAddress: String
    var environment: String
    var project: String
    var testServerAddress: String
    var testEnvironment: String
    var testProject: String
    var lastLogin: String?

    var techLogin: String
    var techPassword: String

    var lastTK: String?
    var lastPersonnelNumber: String?
    var lastPersonnelFullName: String?
    fun getCurrentServerAddress(): String
    fun getCurrentEnvironment(): String
    fun getCurrentProject(): String


}

data class DefaultConnectionSettings(
        val serverAddress: String,
        val environment: String,
        val project: String,
        val testServerAddress: String,
        val testEnvironment: String,
        val testProject: String,
        val techLogin: String,
        val techPassword: String
)