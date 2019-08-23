package com.lenta.shared.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lenta.shared.utilities.Logg

class DefaultSettingsManager(
        private val defaultConnectionSettings: DefaultConnectionSettings,
        private val appSettings: IAppSettings,
        private val sharedPreferences: SharedPreferences,
        private val gson: Gson
) {

    fun isDefaultSettingsChanged(): Boolean {


        val jsonDefSettings = sharedPreferences.getString(KEY_FOR_LAST_DEFAULT_SETTINGS, null)
        if (!appSettings.isConnectionSettingsWasChangedByUser() || jsonDefSettings == null) {
            return false.also {
                saveLastDefaultSettingsToSettings()
            }

        }

        var savedDefaultSettings: DefaultConnectionSettings? = null

        try {
            savedDefaultSettings = gson.fromJson(jsonDefSettings, DefaultConnectionSettings::class.java)
        } catch (e: JsonSyntaxException) {
            Logg.e { "not expected exception: $e" }
            sharedPreferences.edit().remove(KEY_FOR_LAST_DEFAULT_SETTINGS).apply()
        }

        return savedDefaultSettings != defaultConnectionSettings
    }

    fun setNewDefaultSettings() {
        appSettings.cleanFmpSettings()
        saveLastDefaultSettingsToSettings()
    }

    @SuppressLint("ApplySharedPref")
    fun saveLastDefaultSettingsToSettings() {
        sharedPreferences.edit().putString(KEY_FOR_LAST_DEFAULT_SETTINGS, gson.toJson(defaultConnectionSettings)).commit()
    }


    companion object {
        private const val KEY_FOR_LAST_DEFAULT_SETTINGS = "KEY_FOR_LAST_DEFAULT_SETTINGS"
    }

}