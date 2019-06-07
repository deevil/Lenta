package com.lenta.shared.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.IAnalytics
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuViewModel
import com.lenta.shared.features.fmp_settings.FmpSettingsViewModel
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.features.printer_change.PrinterChangeViewModel
import com.lenta.shared.features.select_oper_mode.SelectOperModeViewModel
import com.lenta.shared.features.settings.SettingsViewModel
import com.lenta.shared.features.support.SupportViewModel
import com.lenta.shared.features.tech_login.TechLoginViewModel
import com.lenta.shared.features.test_environment.PinCodeViewModel
import com.lenta.shared.platform.activity.ForegroundActivityProvider
import com.lenta.shared.platform.battery_state.BatteryStateMonitor
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.platform.network_state.NetworkStateMonitor
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.scan.IScanHelper
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.DataBindingExtHolder
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.HyperHiveState
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
    fun getISharedStringResourceManager(): ISharedStringResourceManager
    fun getIAppSettings(): IAppSettings
    fun getIGoBackNavigator(): ICoreNavigator
    fun getHyperHiveState(): HyperHiveState
    fun getHyperHive(): HyperHive
    fun getIFailureInterpreter(): IFailureInterpreter
    fun getISessionInfo(): ISessionInfo
    fun getIAnalytics(): IAnalytics
    fun getIScanHelper(): IScanHelper


    fun inject(it: FmpSettingsViewModel)
    fun inject(it: MessageViewModel)
    fun inject(it: PrinterChangeViewModel)
    fun inject(it: SupportViewModel)
    fun inject(it: PinCodeViewModel)
    fun inject(it: TechLoginViewModel)
    fun inject(it: SelectOperModeViewModel)
    fun inject(it: SettingsViewModel)
    fun inject(it: AuxiliaryMenuViewModel)
    fun inject(dataBindingHelpHolder: DataBindingExtHolder)
}