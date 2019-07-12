package com.lenta.shared.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lenta.shared.BuildConfig
import com.lenta.shared.account.Authenticator
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.account.SessionInfo
import com.lenta.shared.analytics.FmpAnalytics
import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.analytics.db.dao.LogDao
import com.lenta.shared.analytics.db.LogDatabase
import com.lenta.shared.exception.CoreFailureInterpreter
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.navigation.BackFragmentResultHelper
import com.lenta.shared.platform.navigation.CoreNavigator
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.resources.SharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.time.TimeMonitor
import com.lenta.shared.progress.CoreProgressUseCaseInformator
import com.lenta.shared.progress.IProgressUseCaseInformator
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.scan.mobilbase.MobilBaseScanHelper
import com.lenta.shared.settings.AppSettings
import com.lenta.shared.settings.DefaultConnectionSettings
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.prepareFolder
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.HyperHiveState
import com.mobrun.plugin.api.VersionAPI
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class CoreModule(val application: Application, val defaultConnectionSettings: DefaultConnectionSettings) {

    var dbPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).canonicalPath}/FMP/db"

    @Provides
    fun provideAppContext() = application.applicationContext!!

    @Provides
    @Singleton
    internal fun provideAuthenticator(hyperHive: HyperHive): IAuthenticator {
        return Authenticator(hyperHive)
    }

    @Provides
    @Singleton
    internal fun provideIAppSettings(sharedPreferences: SharedPreferences): IAppSettings {
        return AppSettings(sharedPreferences, defaultConnectionSettings)
    }

    @Provides
    @Singleton
    internal fun provideHyperHiveState(appContext: Context, appSettings: IAppSettings): HyperHiveState {
        prepareFolder(dbPath)
        val fmpDbName = "resources_${appSettings.getCurrentEnvironment()}_${appSettings.getCurrentProject()}.sqlite"
        dbPath = "$dbPath/$fmpDbName"

        Logg.d { "dbPath: $dbPath" }
        return HyperHiveState(appContext)
                .setHostWithSchema(appSettings.getCurrentServerAddress())
                .setApiVersion(VersionAPI.V_1)
                .setEnvironmentSlug(appSettings.getCurrentEnvironment())
                .setDbPathDefault(dbPath)
                .setProjectSlug(appSettings.getCurrentProject())
                .setVersionProject("app")
                .setHandler(Handler())
                .setDefaultRetryCount(6)
                .setDefaultRetryIntervalSec(10)
                .setGsonForParcelPacker(GsonBuilder().excludeFieldsWithoutExposeAnnotation().create())
    }


    @Provides
    @Singleton
    internal fun provideHyperHive(hyperHiveState: HyperHiveState): HyperHive {
        val hyperHive = hyperHiveState
                .buildHyperHive()

        if (BuildConfig.DEBUG) {
            Logg.d { "hhive plugin version: ${hyperHive.stateAPI.versionPlugin}" }
            Logg.d { "hhive core version: ${hyperHive.stateAPI.getVersionCoreAPI(0)}" }
            hyperHive.loggingAPI.setLogLevel(0)
        }
        hyperHive.loggingAPI.setLogLevel(10)

        return hyperHive
    }


    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideForegroundActivityProvider() = ForegroundActivityProvider()

    @Provides
    @Singleton
    fun provideNetworkStateMonitor() = NetworkStateMonitor()

    @Provides
    @Singleton
    fun provideINetworkStateMonitor(networkStateReceiver: NetworkStateMonitor): INetworkStateMonitor = networkStateReceiver

    @Provides
    @Singleton
    fun provideBatteryStateMonitor() = BatteryStateMonitor()

    @Provides
    @Singleton
    fun provideIBatteryStateMonitor(batteryStateMonitor: BatteryStateMonitor): IBatteryStateMonitor = batteryStateMonitor

    @Provides
    @Singleton
    fun provideITimeMonitor(): ITimeMonitor = TimeMonitor(intervalInMsec = 5000)

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    internal fun provideSharedStringResourceManager(context: Context): ISharedStringResourceManager {
        return SharedStringResourceManager(context)
    }

    @Provides
    @Singleton
    internal fun provideCoreFailureInterpreter(context: Context): IFailureInterpreter {
        return CoreFailureInterpreter(context)
    }


    @Provides
    @Singleton
    internal fun provideICoreNavigator(context: Context,
                                       foregroundActivityProvider: ForegroundActivityProvider,
                                       failureInterpreter: IFailureInterpreter,
                                       analytics: IAnalytics,
                                       backFragmentResultHelper: BackFragmentResultHelper): ICoreNavigator {
        return CoreNavigator(context, foregroundActivityProvider, failureInterpreter, analytics, backFragmentResultHelper)
    }

    @Provides
    @Singleton
    internal fun provideISessionInfo(): ISessionInfo {
        return SessionInfo()
    }

    @Provides
    @Singleton
    internal fun provideIAnalitycs(hyperHive: HyperHive, logDao: LogDao): IAnalytics {
        return FmpAnalytics(hyperHive, logDao)
    }

    @Provides
    @Singleton
    internal fun provideScanHelper(): IScanHelper {
        return MobilBaseScanHelper()
    }

    @Provides
    @Singleton
    internal fun provideIProgressUseCaseInformator(context: Context): IProgressUseCaseInformator {
        return CoreProgressUseCaseInformator(context)
    }

    @Provides
    fun provideScanInfoRequest(hyperHive: HyperHive, gson: Gson, sessionInfo: ISessionInfo): ScanInfoRequest {
        return ScanInfoRequest(hyperHive, gson, sessionInfo)
    }

    @Provides
    @Singleton
    internal fun provideBackResultHelper(): BackFragmentResultHelper {
        return BackFragmentResultHelper()
    }

    @Provides
    @Singleton
    fun provideLogDao(context: Context): LogDao {
        prepareFolder(dbPath)
        val dbLogName = "loggs.sqlite"
        return Room.databaseBuilder(
                context,
                LogDatabase::class.java, "$dbPath/$dbLogName"
        ).build().logDao()
    }

}