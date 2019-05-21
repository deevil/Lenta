package com.lenta.shared.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.navigation.CoreNavigator
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.resources.IStringResourceManager
import com.lenta.shared.platform.resources.StringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.time.TimeMonitor
import com.lenta.shared.settings.AppSettings
import com.lenta.shared.settings.IAppSettings
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class CoreModule(val application: Application) {
    @Provides
    fun provideAppContext() = application.applicationContext!!

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
    internal fun provideStringResourceManager(context: Context): IStringResourceManager {
        return StringResourceManager(context)
    }

    @Provides
    @Singleton
    internal fun provideIAppSettings(sharedPreferences: SharedPreferences): IAppSettings {
        return AppSettings(sharedPreferences)
    }

    @Provides
    @Singleton
    internal fun provideIGoBackNavigator(foregroundActivityProvider: ForegroundActivityProvider): ICoreNavigator {
        return CoreNavigator(foregroundActivityProvider)
    }

}