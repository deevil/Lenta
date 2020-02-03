package com.lenta.shared.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ApplySharedPref")
class AppSettings(
        val sharedPrefferences: SharedPreferences,
        val defaultConnectionSettings: DefaultConnectionSettings
) : IAppSettings {

    override var isTest: Boolean
        get() = sharedPrefferences.getBoolean(FmpSettingsKey.fmpSettings_isTest.name, false)
        set(value) {
            sharedPrefferences.edit().putBoolean(FmpSettingsKey.fmpSettings_isTest.name, value).commit()
        }

    override var serverAddress: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_serverAddress.name, defaultConnectionSettings.serverAddress)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_serverAddress.name, value).commit()
        }
    override var environment: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_environment.name, defaultConnectionSettings.environment)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_environment.name, value).commit()
        }
    override var project: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_project.name, defaultConnectionSettings.project)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_project.name, value).commit()
        }

    override var testServerAddress: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_test_serverAddress.name, defaultConnectionSettings.testServerAddress)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_test_serverAddress.name, value).commit()
        }
    override var testEnvironment: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_test_environment.name, defaultConnectionSettings.testEnvironment)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_test_environment.name, value).commit()
        }
    override var testProject: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_test_project.name, defaultConnectionSettings.testProject)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_test_project.name, value).commit()
        }

    override var techLogin: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_tech_login.name, defaultConnectionSettings.techLogin)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_tech_login.name, value).commit()
        }

    override var techPassword: String
        get() = sharedPrefferences.getString(FmpSettingsKey.fmpSettings_tech_password.name, defaultConnectionSettings.techPassword)
        set(value) {
            sharedPrefferences.edit().putString(FmpSettingsKey.fmpSettings_tech_password.name, value).commit()
        }


    override var printer: String?
        get() = sharedPrefferences.getString("printer", null)
        set(value) {
            sharedPrefferences.edit().putString("printer", value).commit()
        }

    override var printerNotVisible: Boolean = false


    override var printerNumber: String?
        get() = sharedPrefferences.getString("printerNumber", null)
        set(value) {
            sharedPrefferences.edit().putString("printerNumber", value).commit()
            printerNumberLiveData.value = value
        }

    override var weightEquipmentName: String?
        get() = sharedPrefferences.getString("weightEquipmentName", null)
        set(value) {
            sharedPrefferences.edit().putString("weightEquipmentName", value).commit()
        }

    override var printerIpAddress: String?
        get() = sharedPrefferences.getString("printerIpAddress", null)
        set(value) {
            sharedPrefferences.edit().putString("printerIpAddress", value).commit()
        }

    override var lastLogin: String?
        get() = sharedPrefferences.getString("lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastLogin", value).commit()
        }

    override var lastJobType: String?
        get() = sharedPrefferences.getString("lastJobType$lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastJobType$lastLogin", value).commit()
        }

    override var lastTK: String?
        get() = sharedPrefferences.getString("lastTK$lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastTK$lastLogin", value).commit()
        }

    override var lastPersonnelNumber: String?
        get() = sharedPrefferences.getString("lastPersonnelNumber$lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastPersonnelNumber$lastLogin", value).commit()
        }

    override var lastPersonnelFullName: String?
        get() = sharedPrefferences.getString("lastPersonnelFullName$lastLogin", null)
        set(value) {
            sharedPrefferences.edit().putString("lastPersonnelFullName$lastLogin", value).commit()
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

    override val printerNumberLiveData: MutableLiveData<String?> = MutableLiveData(printerNumber)

    override fun cleanFmpSettings() {
        sharedPrefferences.edit().let { editor ->
            FmpSettingsKey.values().forEach {
                editor.remove(it.name)
            }
            editor.commit()
        }
    }

    override fun isConnectionSettingsWasChangedByUser(): Boolean {
        return isTest && sharedPrefferences.contains(Companion.FmpSettingsKey.fmpSettings_serverAddress.name)
    }

    companion object {
        private enum class FmpSettingsKey {
            fmpSettings_isTest,
            fmpSettings_serverAddress,
            fmpSettings_environment,
            fmpSettings_project,
            fmpSettings_test_serverAddress,
            fmpSettings_test_environment,
            fmpSettings_test_project,
            fmpSettings_tech_login,
            fmpSettings_tech_password
        }
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
    var lastJobType: String?

    var printer: String?
    var printerNotVisible: Boolean
    var printerNumber: String?
    val printerNumberLiveData: MutableLiveData<String?>

    var weightEquipmentName: String?
    var printerIpAddress: String?

    var techLogin: String
    var techPassword: String

    var lastTK: String?
    var lastPersonnelNumber: String?
    var lastPersonnelFullName: String?

    fun getCurrentServerAddress(): String
    fun getCurrentEnvironment(): String
    fun getCurrentProject(): String
    fun cleanFmpSettings()
    fun isConnectionSettingsWasChangedByUser(): Boolean
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