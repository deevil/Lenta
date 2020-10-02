package com.lenta.shared.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ApplySharedPref")
class AppSettings(
        private val sharedPreferences: SharedPreferences,
        private val defaultConnectionSettings: DefaultConnectionSettings
) : IAppSettings {

    override var isTest: Boolean
        get() = sharedPreferences.getBoolean(FmpSettingsKey.fmpSettings_isTest.name, false)
        set(value) {
            sharedPreferences.edit().putBoolean(FmpSettingsKey.fmpSettings_isTest.name, value).commit()
        }

    override var serverAddress: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_serverAddress.name, defaultConnectionSettings.serverAddress).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_serverAddress.name, value).commit()
        }
    override var environment: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_environment.name, defaultConnectionSettings.environment).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_environment.name, value).commit()
        }
    override var project: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_project.name, defaultConnectionSettings.project).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_project.name, value).commit()
        }

    override var testServerAddress: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_test_serverAddress.name, defaultConnectionSettings.testServerAddress).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_test_serverAddress.name, value).commit()
        }
    override var testEnvironment: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_test_environment.name, defaultConnectionSettings.testEnvironment).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_test_environment.name, value).commit()
        }
    override var testProject: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_test_project.name, defaultConnectionSettings.testProject).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_test_project.name, value).commit()
        }

    override var techLogin: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_tech_login.name, defaultConnectionSettings.techLogin).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_tech_login.name, value).commit()
        }

    override var techPassword: String
        get() = sharedPreferences.getString(FmpSettingsKey.fmpSettings_tech_password.name, defaultConnectionSettings.techPassword).orEmpty()
        set(value) {
            sharedPreferences.edit().putString(FmpSettingsKey.fmpSettings_tech_password.name, value).commit()
        }


    override var printer: String?
        get() = sharedPreferences.getString("printer", null)
        set(value) {
            sharedPreferences.edit().putString("printer", value).commit()
        }

    override var printerNotVisible: Boolean = false


    override var printerNumber: String?
        get() = sharedPreferences.getString("printerNumber", null)
        set(value) {
            sharedPreferences.edit().putString("printerNumber", value).commit()
            printerNumberLiveData.value = value
        }

    override var weightEquipmentName: String?
        get() = sharedPreferences.getString("weightEquipmentName", null)
        set(value) {
            sharedPreferences.edit().putString("weightEquipmentName", value).commit()
        }

    override var printerIpAddress: String?
        get() = sharedPreferences.getString("printerIpAddress", null)
        set(value) {
            sharedPreferences.edit().putString("printerIpAddress", value).commit()
        }

    override var lastLogin: String?
        get() = sharedPreferences.getString("lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastLogin", value).commit()
        }

    override var lastJobType: String?
        get() = sharedPreferences.getString("lastJobType$lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastJobType$lastLogin", value).commit()
        }

    override var lastTK: String?
        get() = sharedPreferences.getString("lastTK$lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastTK$lastLogin", value).commit()
        }
    override var lastGroup: String?
        get() = sharedPreferences.getString("lastGroup$lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastGroup$lastLogin", value).commit()
        }

    override var warehouseReceiverPosition: Int?
        get() = sharedPreferences.getInt("warehouseReceiverPosition$lastLogin", 0)
        set(value) {
            sharedPreferences.edit().putInt("warehouseReceiverPosition$lastLogin", value
                    ?: 0).commit()
        }

    override var warehouseSenderPosition: Int?
        get() = sharedPreferences.getInt("warehouseSenderPosition$lastLogin", 0)
        set(value) {
            sharedPreferences.edit().putInt("warehouseSenderPosition$lastLogin", value
                    ?: 0).commit()
        }

    override var lastPersonnelNumber: String?
        get() = sharedPreferences.getString("lastPersonnelNumber$lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastPersonnelNumber$lastLogin", value).commit()
        }

    override var lastPersonnelFullName: String?
        get() = sharedPreferences.getString("lastPersonnelFullName$lastLogin", null)
        set(value) {
            sharedPreferences.edit().putString("lastPersonnelFullName$lastLogin", value).commit()
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
        sharedPreferences.edit().let { editor ->
            FmpSettingsKey.values().forEach {
                editor.remove(it.name)
            }
            editor.commit()
        }
    }

    override fun isConnectionSettingsWasChangedByUser(): Boolean {
        return isTest && sharedPreferences.contains(Companion.FmpSettingsKey.fmpSettings_serverAddress.name)
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
    var lastGroup: String?
    var lastPersonnelNumber: String?
    var lastPersonnelFullName: String?

    var warehouseSenderPosition: Int?
    var warehouseReceiverPosition: Int?

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