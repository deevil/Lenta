package com.lenta.bp7.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lenta.bp7.AndroidApplication
import com.lenta.shared.settings.AppSettings
import com.lenta.shared.settings.DefaultConnectionSettings
import com.lenta.shared.settings.IAppSettings
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.HyperHiveState
import com.mobrun.plugin.api.VersionAPI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PersistCheckResultTest {

    private lateinit var persistCheckResult: IPersistCheckResult

    @BeforeEach
    fun createPersistCheckResult() {
        val testDbPath = ""
        val context: Context = AndroidApplication.getContext()
        val defaultConnectionSettings: DefaultConnectionSettings = AndroidApplication.getDefaultConnectionSettings()
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val appSettings: IAppSettings = AppSettings(sharedPreferences, defaultConnectionSettings)

        val hyperHiveState: HyperHiveState = HyperHiveState(context)
                .setHostWithSchema(appSettings.getCurrentServerAddress())
                .setApiVersion(VersionAPI.V_1)
                .setEnvironmentSlug(appSettings.getCurrentEnvironment())
                //.setDbPathDefault("${Constants.DB_PATH}/$fmpDbName")
                .setDbPathDefault(testDbPath)
                .setProjectSlug(appSettings.getCurrentProject())
                .setVersionProject("app")
                .setHandler(Handler())
                .setDefaultRetryCount(6)
                .setDefaultRetryIntervalSec(10)
                .setGsonForParcelPacker(GsonBuilder().excludeFieldsWithoutExposeAnnotation().create())

        val hyperHive: HyperHive = hyperHiveState.buildHyperHive()
        val gson: Gson = GsonBuilder().create()
        persistCheckResult = PersistCheckResult(hyperHive, gson)
    }

    @Test
    fun saveCheckResult() {
    }

    @Test
    fun getSavedCheckResult() {
    }

    @Test
    fun clearSavedData() {
    }
}