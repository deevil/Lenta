package com.lenta.shared.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lenta.shared.features.fmp_settings.FmpSettingsViewModel
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.resources.IStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.settings.IAppSettings
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [CoreModule::class])
interface CoreComponent {
    fun getSharedPreferences(): SharedPreferences
    fun getAppContext(): Context
    fun getForegroundActivityProvider(): ForegroundActivityProvider
    fun getNetworkStateMonitor(): NetworkStateMonitor
    fun getINetworkStateMonitor(): INetworkStateMonitor
    fun getBatteryStateMonitor(): BatteryStateMonitor
    fun getIBatteryStateMonitor(): IBatteryStateMonitor
    fun getTimeMonitor(): ITimeMonitor
    fun getGson(): Gson
    fun getIStringResourceManager(): IStringResourceManager
    fun getIAppSettings(): IAppSettings
    fun getIGoBackNavigator(): ICoreNavigator
    fun inject(it: FmpSettingsViewModel)
}